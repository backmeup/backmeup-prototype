package org.backmeup.job.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

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
	      
	      addStatusToDb(new Status(persistentJob, "BackupJob Started", "info", new Date()));
	      
	      for (ProfileOptions po : persistentJob.getSourceProfiles()) {
	    	storage.open(generateTmpDirName (job, po));
	    	  
	        Datasource source = plugins.getDatasource(po.getProfile()
	            .getDescription());
	        
	        Properties sourceProperties = authenticationData.getByProfileId(po
	            .getProfile().getProfileId());
	        
	    	addStatusToDb(new Status(persistentJob, "Downloading from " + po.getProfile().getProfileName(), "info", new Date()));
	    	
	    	// Download from source
	        try {
	          source.downloadAll(sourceProperties, storage, new JobStatusProgressor(persistentJob));
	        } catch (StorageException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", new Date()));
	        } catch (DatasourceException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", new Date()));
	        }
	        
	        addStatusToDb(new Status(persistentJob, "Download completed", "info", new Date()));
	        
	        // Execute Actions in sequence
	        for (ActionProfile actionProfile : persistentJob.getRequiredActions()) {
	        	String actionId = actionProfile.getActionId();
	        	try {
		        	Action action = null;
		        	Properties params = new Properties();
		        	
		        	if ("org.backmeup.filesplitting".equals(actionId)) {
		        		action = new FilesplittAction();
		        	} else if ("org.backmeup.indexer".equals(actionId)) {
		        		Configuration config = Configuration.getConfig();
		        		String host = config.getProperty(INDEX_HOST);
		        		int port = Integer.parseInt(config.getProperty(INDEX_PORT));
		        		
		        		Client client = new TransportClient()
		    				.addTransportAddress(new InetSocketTransportAddress(host, port));
		        		
		        		action = new IndexAction(client);
		        	} else if ("org.backmeup.encryption".equals(actionId)) {
		        		action = new EncryptionAction();
		        	} else {
		        		addStatusToDb(new Status(persistentJob, "Unsupported Action: " + actionId, "error", new Date()));
		        	}
	        	
		        	if (action != null)
		        		action.doAction(params, storage, job, new JobStatusProgressor(persistentJob));
	        	} catch (ActionException e) {
	        		addStatusToDb(new Status(persistentJob, e.getMessage(), "error", new Date()));
	        	}
	        }    
	
	        try {
	        	// Upload to Sink
	        	addStatusToDb(new Status(persistentJob, "Uploading to " + 
	        		persistentJob.getSinkProfile().getProfileName(), "info", new Date()));
	
	        	sink.upload(sinkProperties, storage, new JobStatusProgressor(persistentJob));
	        } catch (StorageException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", new Date()));
	        }
	        
	        addStatusToDb(new Status(persistentJob, "BackupJob Completed", "info", new Date()));
	      
	        storage.close();
	      }
	      
	    } catch (StorageException e) {
	    	addStatusToDb(new Status(persistentJob, e.getMessage(), "error", new Date()));
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
		String profilename = po.getProfile ().getDescription ();

		formatter = new SimpleDateFormat (formatstring.replaceAll ("%PROFILEID%", profileid.toString ()).replaceAll ("%SOURCE%", profilename));
		
		return conftempdir + "/" + jobid + "/" + formatter.format (date);
	}
  
  private void testActions(BackupJob job, String tmpDir) {
      // TODO remove this. Created by ft only for actionPlugin tests
      System.out.println ("######################################################");
      System.out.println ("Test action Plugins");
      System.out.println ("######################################################");
      try
      {
      	executeActions (job, tmpDir);
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
	private void executeActions (BackupJob job, String tmpDir) throws ActionException, StorageException
	{
		if (job.getUser ().getUsername ().equals ("ft@x-net.at") == false)
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
		
		Storage storage = new LocalFilesystemStorage ();
		
		storage.open (tmpDir);
		
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
		
		public JobStatusProgressor(BackupJob job) {
			this.job = job;
		}

		@Override
		public void progress(String message) {
			addStatusToDb(new Status(job, message, "info", new Date()));
		}
		
	}

}
