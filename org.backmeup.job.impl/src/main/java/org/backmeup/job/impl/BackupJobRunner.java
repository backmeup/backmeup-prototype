package org.backmeup.job.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.backmeup.configuration.Configuration;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.StatusDao;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Status;
import org.backmeup.model.Token;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.actions.encryption.EncryptionAction;
import org.backmeup.plugin.api.actions.filesplitting.FilesplittAction;
import org.backmeup.plugin.api.actions.indexing.IndexAction;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

/**
 * Implements the actual BackupJob execution.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at.>
 */
public class BackupJobRunner {
	
  private static final String INDEX_HOST = "index.host";
  private static final String INDEX_PORT = "index.port";

  private Plugin plugins;
  private Keyserver keyserver;
  private Connection conn;
  private DataAccessLayer dal;

  public BackupJobRunner(Plugin plugins, Keyserver keyserver, Connection conn,
      DataAccessLayer dal) {
    this.plugins = plugins;
    this.keyserver = keyserver;
    this.conn = conn;
    this.dal = dal;
  }

  private void addStatusToDb(Status status) {
	System.out.println("STATUS: " + status.getMessage());
    conn.beginOrJoin();    
    StatusDao sd = dal.createStatusDao();
    sd.save(status); // store job within database
    conn.commit(); // commit to the database and to the core
  }

  public void executeBackup(BackupJob job, Storage storage) {
    try {
      conn.beginOrJoin();
      BackupJobDao bjd = dal.createBackupJobDao();
      
      // use the job which is stored within the database      
      BackupJob persistentJob = bjd.merge(job);             
      
      // when will the next access to the access data occur? current time +
      // delay
      persistentJob.getToken().setBackupdate(new Date().getTime() + persistentJob.getDelay());
      
      // get access data + new token for next access
      AuthDataResult authenticationData = keyserver.getData(persistentJob.getToken());
      
      // the token for the next getData call
      Token newToken = authenticationData.getNewToken();
      persistentJob.setToken(newToken);
      
      // TODO: store newToken for the next backup schedule
      conn.commit(); // stores the new token within database
      
      // Open temporary storage
      try {
	      Datasink sink = plugins.getDatasink(persistentJob.getSinkProfile().getDescription());
	      Properties sinkProperties = 
	    		  authenticationData.getByProfileId(persistentJob.getSinkProfile().getProfileId());
	      
	      addStatusToDb(new Status(persistentJob, "BackupJob Started", "info", "backupjob", new Date()));
	      
	      for (ProfileOptions po : persistentJob.getSourceProfiles()) {
	    	String tmpDir = generateTmpDirName (job, po);
	    	storage.open(tmpDir);
	    	  
	        Datasource source = plugins.getDatasource(po.getProfile()
	            .getDescription());
	        
	        Properties sourceProperties = authenticationData.getByProfileId(po
	            .getProfile().getProfileId());
	        
	    	addStatusToDb(new Status(persistentJob, "Downloading from " + po.getProfile().getProfileName(), "info", "datasource", new Date()));
	    	
	    	// Download from source
	        try {
	          source.downloadAll(sourceProperties, storage, new JobStatusProgressor(persistentJob, "datasource"));
	        } catch (StorageException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", "datasource", new Date()));
	        } catch (DatasourceException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", "datasource", new Date()));
	        }
	        
	        addStatusToDb(new Status(persistentJob, "Download completed", "info", "datasource", new Date()));
	        
	        // make properties global for the action loop. So the plugins can communicate (filesplitt + encryption)
	        Properties params = new Properties();
	        
	        // Execute Actions in sequence
	        for (ActionProfile actionProfile : persistentJob.getRequiredActions()) {
	        	String actionId = actionProfile.getActionId();
	        	
	        	try {   
		        	Action action = null;
		        	
		        	if ("org.backmeup.filesplitting".equals(actionId)) {
		        		action = new FilesplittAction();
		        		action.doAction(params, storage, job, new JobStatusProgressor(persistentJob, "filesplittaction"));
		        	} else if ("org.backmeup.indexer".equals(actionId)) {
		        		Configuration config = Configuration.getConfig();
		        		String host = config.getProperty(INDEX_HOST);
		        		int port = Integer.parseInt(config.getProperty(INDEX_PORT));
		        		
		        		Client client = new TransportClient()
		        			.addTransportAddress(new InetSocketTransportAddress(host, port));
		        		
		        		action = new IndexAction(client);
		        		action.doAction(params, storage, persistentJob, new JobStatusProgressor(persistentJob, "indexaction"));
		        		
		        		client.close();
		          	} else if ("org.backmeup.encryption".equals(actionId)) {
		        		action = new EncryptionAction();
		        		action.doAction(params, storage, job, new JobStatusProgressor(persistentJob, "encryptionaction"));
		        	} else {
		        		addStatusToDb(new Status(persistentJob, "Unsupported Action: " + actionId, "error", "backupjob", new Date()));
		        	}
	        	} catch (ActionException e) {
	        		addStatusToDb(new Status(persistentJob, e.getMessage(), "error", "backupjob", new Date()));
	        	}
	        }    
	        
	        try {
	        	// Upload to Sink
	        	addStatusToDb(new Status(persistentJob, "Uploading to " + 
	        		persistentJob.getSinkProfile().getProfileName(), "info", "datasink", new Date()));
	
	        	sinkProperties.setProperty ("org.backmeup.tmpdir", getLastSplitElement (tmpDir, "/"));
	        	sink.upload(sinkProperties, storage, new JobStatusProgressor(persistentJob, "datasink"));
	        } catch (StorageException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", "datasink", new Date()));
	        }
	        
	        addStatusToDb(new Status(persistentJob, "BackupJob Completed", "info", "backupjob", new Date()));
	      
	        storage.close();
	      }
	      
	    } catch (StorageException e) {
	    	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", "storage", new Date()));
	    }
    } finally {
      conn.rollback();
    }
  }
  
	private String generateTmpDirName (BackupJob job, ProfileOptions po)
	{
		String conftempdir = Configuration.getConfig ().getProperty ("job.temporaryDirectory");
		String formatstring = Configuration.getConfig ().getProperty ("job.backupname");
		SimpleDateFormat formatter = null;
		Date date = new Date ();

		Long profileid = po.getProfile ().getProfileId ();
		Long jobid = job.getId ();
		// Take only last part of "org.backmeup.xxxx" (xxxx)
		String profilename = getLastSplitElement (po.getProfile ().getDescription (), "\\.");
		
		formatter = new SimpleDateFormat (formatstring.replaceAll ("%PROFILEID%", profileid.toString ()).replaceAll ("%SOURCE%", profilename));
		
		return conftempdir + "/" + jobid + "/" + formatter.format (date);
	}
	
	private String getLastSplitElement (String text, String regex)
	{
		String[] parts = text.split (regex);
		
		if (parts.length > 0)
		{
			return parts[parts.length - 1];
		}
		else
		{
			return text;
		}
	}
  
  private void testActions(BackupJob job, Storage storage) {
      // TODO remove this. Created by ft only for actionPlugin tests
      System.out.println ("######################################################");
      System.out.println ("Test action Plugins");
      System.out.println ("######################################################");
      try
      {
      	executeActions (job, storage);
      }
      catch (ActionException e)
      {
      	System.out.println("ERROR: " + e.getMessage());
      }
      catch (StorageException e)
		{
			// TODO error handling
			System.out.println ("ERROR: " + e.getMessage ());
		}
      System.out.println ("######################################################");	  
  }
  
	// TODO remove this. Created by ft only for actionPlugin tests
	private void executeActions (BackupJob job, Storage storage) throws ActionException, StorageException
	{
		if (job.getUser ().getUsername ().equals ("irgend@x-net.at") == false)
		{
			return;
		}
		
		Action filesplitter = new FilesplittAction ();

		Progressable progressor = new Progressable ()
		{
			@Override
			public void progress (String message)
			{
				System.out.println (message);
			}
		};
		
		Properties parameters = new Properties ();
		
		System.out.println ("######################################################");
		System.out.println ("Filesplitter");
		System.out.println ("######################################################");
		filesplitter.doAction (parameters, storage, job, progressor);
		
		System.out.println ("######################################################");
		System.out.println ("Encryption");
		System.out.println ("######################################################");
		Action encryption = new EncryptionAction ();
		
		parameters.setProperty ("org.backmeup.encryption.password", "Test1234!");
		encryption.doAction (parameters, storage, job, progressor);
		
		storage.close ();
	}
	
	private class JobStatusProgressor implements Progressable {
		
		private BackupJob job;
		private String category;
		
		public JobStatusProgressor(BackupJob job, String category) {
			this.job = job;
			this.category = category;
		}

		@Override
		public void progress(String message) {
			addStatusToDb(new Status(job, message, "info", category, new Date()));
		}
		
	}

}
