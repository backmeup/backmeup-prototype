package org.backmeup.job.impl;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.backmeup.configuration.Configuration;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.JobProtocolDao;
import org.backmeup.dal.StatusDao;
import org.backmeup.job.impl.threadbased.ThreadbasedJobManager;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.JobProtocol;
import org.backmeup.model.JobProtocol.JobProtocolMember;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Status;
import org.backmeup.model.StatusCategory;
import org.backmeup.model.StatusType;
import org.backmeup.model.Token;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.actions.encryption.EncryptionAction;
import org.backmeup.plugin.api.actions.filesplitting.FilesplittAction;
import org.backmeup.plugin.api.actions.indexing.IndexAction;
import org.backmeup.plugin.api.actions.thumbnail.ThumbnailAction;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.api.connectors.DatasourceException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.utilities.mail.Mailer;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * Implements the actual BackupJob execution.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at.>
 */
public class BackupJobRunner {
	
  private static final String ERROR_EMAIL_TEXT = "ERROR_EMAIL_TEXT";
  private static final String ERROR_EMAIL_SUBJECT = "ERROR_EMAIL_SUBJECT";
  private static final String ERROR_EMAIL_MIMETYPE = "ERROR_EMAIL_MIMETYPE";
  private static final String INDEX_HOST = "index.host";
  private static final String INDEX_PORT = "index.port";

  private Plugin plugins;
  private Keyserver keyserver;
  private Connection conn;
  private DataAccessLayer dal;
  
  private ResourceBundle textBundle = ResourceBundle
      .getBundle(BackupJobRunner.class.getSimpleName());

  public BackupJobRunner(Plugin plugins, Keyserver keyserver, Connection conn,
      DataAccessLayer dal) {
    this.plugins = plugins;
    this.keyserver = keyserver;
    this.conn = conn;
    this.dal = dal;
  }

  private Status addStatusToDb(Status status) {
	System.out.println("STATUS: " + status.getMessage());
    conn.beginOrJoin();    
    StatusDao sd = dal.createStatusDao();
    sd.save(status); // store job within database    
    conn.commit(); // commit to the database and to the core
    return status;
  }
  
  private void deleteOldStatus(BackupJob persistentJob) {
    conn.beginOrJoin();
    StatusDao sd = dal.createStatusDao();
    sd.deleteBefore(persistentJob.getId(), new Date());
    conn.commit();    
  }
  
  private void storeJobProtocol(BackupJob job, JobProtocol protocol, int storedEntriesCount, boolean success) {
    conn.beginOrJoin();
    BackupJobDao jobDao = dal.createBackupJobDao();
    job = jobDao.merge(job);
    JobProtocolDao jpd = dal.createJobProtocolDao();
    // remove old entries, then store the new one 
    jpd.deleteByUsername(job.getUser().getUsername());
    protocol.setUser(job.getUser());
    protocol.setJob(job);
    protocol.setSuccessful(success);
    protocol.setTotalStoredEntries(storedEntriesCount);
    jpd.save(protocol);
    conn.commit();
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
      
      String userEmail = persistentJob.getUser().getEmail();
      String jobName = persistentJob.getJobTitle();
      
      // TODO: store newToken for the next backup schedule
      conn.commit(); // stores the new token within database
      
      // Protocol Overview requires information about executed jobs      
      JobProtocol protocol = new JobProtocol();      
      Set<JobProtocolMember> protocolEntries = new HashSet<JobProtocolMember>();
      protocol.setMembers(protocolEntries);
      protocol.setSinkTitle(persistentJob.getSinkProfile().getProfileName());
      protocol.setExecutionTime(new Date());      
      
      // tracke the error status messages
      List<Status> errorStatus = new ArrayList<Status>();
      
      // Open temporary storage
      try {
	      Datasink sink = plugins.getDatasink(persistentJob.getSinkProfile().getDescription());
	      Properties sinkProperties = 
	    		  authenticationData.getByProfileId(persistentJob.getSinkProfile().getProfileId());
	      
	      // delete previously stored status, as we only need the latest
	      deleteOldStatus(persistentJob);
	      addStatusToDb(new Status(persistentJob, "", StatusType.STARTED, StatusCategory.INFO, new Date()));
	      long previousSize = 0;

	      for (ProfileOptions po : persistentJob.getSourceProfiles()) {
	    	String tmpDir = generateTmpDirName (job, po);
	    	storage.open(tmpDir);
	    	  
	        Datasource source = plugins.getDatasource(po.getProfile()
	            .getDescription());
	        
	        Properties sourceProperties = authenticationData.getByProfileId(po
	            .getProfile().getProfileId());
	        
	    	addStatusToDb(new Status(persistentJob, "", StatusType.DOWNLOADING, StatusCategory.INFO, new Date()));
	    	
	    	// Download from source
	        try {
	            source.downloadAll(sourceProperties, storage, new JobStatusProgressor(persistentJob, "datasource"));
	        } catch (StorageException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), StatusType.DOWNLOAD_FAILED, StatusCategory.WARNING, new Date()));
	        } catch (DatasourceException e) {
	        	addStatusToDb(new Status(persistentJob, e.getMessage(), StatusType.DOWNLOAD_FAILED, StatusCategory.WARNING, new Date()));
	        }
	        	        
	        // for each datasource add an entry with bytes it consumed 
	        long currentSize = storage.getDataObjectSize() - previousSize;
	        protocolEntries.add(new JobProtocolMember(protocol, po.getProfile().getProfileName(), currentSize));
	        previousSize = storage.getDataObjectSize();
	        
	        // make properties global for the action loop. So the plugins can communicate (filesplitt + encryption)
	        Properties params = new Properties();
	        params.putAll(sinkProperties);
	        params.putAll(sourceProperties);
	        
	        // Execute Actions in sequence
	        addStatusToDb(new Status(persistentJob, "", StatusType.PROCESSING, StatusCategory.INFO, new Date()));
	        for (ActionProfile actionProfile : persistentJob.getRequiredActions()) {
	        	String actionId = actionProfile.getActionId();
	        
	        	try {   		        	
		        	if ("org.backmeup.encryption".equals(actionId)) {
		        		// If we do encryption, the Filesplitter needs to run before!
		        		Action filesplitAction = new FilesplittAction();
		        		filesplitAction.doAction(params, storage, persistentJob,  new JobStatusProgressor(persistentJob, "filesplittaction"));
		        			
		        		// After splitting, run encryption
		        		Action encryptionAction = new EncryptionAction();
		        		encryptionAction.doAction(params, storage, job, new JobStatusProgressor(persistentJob, "encryptionaction"));
		        	} else if ("org.backmeup.indexer".equals(actionId)) {
		        		// If we do indexing, the Thumbnail renderer needs to run before!
		        		Action thumbnailAction = new ThumbnailAction();
		        		thumbnailAction.doAction(params, storage, persistentJob, new JobStatusProgressor(persistentJob, "thumbnailAction"));
		        		
		        		// After thumbnail rendering, run indexing
		        		Configuration config = Configuration.getConfig();
		        		String host = config.getProperty(INDEX_HOST);
		        		int port = Integer.parseInt(config.getProperty(INDEX_PORT));
		        		
		        		Client client = new TransportClient()
		        			.addTransportAddress(new InetSocketTransportAddress(host, port));
		        		
		        		Action indexAction = new IndexAction(client);
		        		indexAction.doAction(params, storage, persistentJob, new JobStatusProgressor(persistentJob, "indexaction"));
		        		client.close();
		        	} else {
		        		// Only happens in case Job was corrupted in the core - we'll handle that as a fatal error
		        	  errorStatus.add(addStatusToDb(new Status(persistentJob, "Unsupported Action: " + actionId, StatusType.JOB_FAILED, StatusCategory.ERROR, new Date())));
		        	}
	        	} catch (ActionException e) {
	        		// Should only happen in case of problems in the core (file I/O, DB access, etc.) - we'll handle that as a fatal error
	        	  errorStatus.add(addStatusToDb(new Status(persistentJob, e.getMessage(), StatusType.JOB_FAILED, StatusCategory.ERROR, new Date())));
	        	}
	        }    
	        
	        try {
	        	// Upload to Sink
	        	addStatusToDb(new Status(persistentJob, "", StatusType.UPLOADING, StatusCategory.INFO, new Date()));
	
	        	sinkProperties.setProperty ("org.backmeup.tmpdir", getLastSplitElement (tmpDir, "/"));
	        	sinkProperties.setProperty("org.backmeup.userid", persistentJob.getUser().getUserId() + "");
	        	sink.upload(sinkProperties, storage, new JobStatusProgressor(persistentJob, "datasink"));
		        addStatusToDb(new Status(persistentJob, "", StatusType.SUCCESSFUL, StatusCategory.INFO, new Date()));
	        } catch (StorageException e) {
	          errorStatus.add(addStatusToDb(new Status(persistentJob, e.getMessage(), StatusType.JOB_FAILED, StatusCategory.ERROR, new Date())));
	        }
	        
	        // store job protocol within database
	        storeJobProtocol(persistentJob, protocol, storage.getDataObjectCount(), true);
	        
	        storage.close();
	      }	      
	    } catch (StorageException e) {
	      // job failed, store job protocol within database
        storeJobProtocol(persistentJob, protocol, 0, false);
        errorStatus.add(addStatusToDb(new Status(persistentJob, e.getMessage(), StatusType.JOB_FAILED, StatusCategory.ERROR, new Date())));
	    }
      // send error message, if there were any error status messages
      if (errorStatus.size() > 0) {        
        Mailer.send(userEmail, MessageFormat.format(textBundle.getString(ERROR_EMAIL_SUBJECT), userEmail),
                               MessageFormat.format(textBundle.getString(ERROR_EMAIL_TEXT), userEmail, jobName),
                               textBundle.getString(ERROR_EMAIL_MIMETYPE));
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
