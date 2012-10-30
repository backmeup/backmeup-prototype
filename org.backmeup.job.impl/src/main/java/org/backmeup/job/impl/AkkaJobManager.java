package org.backmeup.job.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.backmeup.dal.BackupJobDao;
import org.backmeup.dal.Connection;
import org.backmeup.dal.DataAccessLayer;
import org.backmeup.dal.UserDao;
import org.backmeup.job.JobManager;
import org.backmeup.keyserver.client.AuthDataResult;
import org.backmeup.keyserver.client.Keyserver;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.Token;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.exceptions.BackMeUpException;

import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import akka.util.Duration;

/**
 * An abstract {@link JobManager} implementation that supports scheduled execution 
 * backed by the Akka actor framework.
 * 
 * Subclasses of this class need to define what should happen when the job is
 * triggered by implementing the 'newJobRunner' method.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
abstract public class AkkaJobManager implements JobManager {
	
	private static final ActorSystem system = ActorSystem.create();
	
	@Inject
	protected Connection conn;
	
	@Inject
	protected DataAccessLayer dal;
	
	@Inject
	private Keyserver keyserver;
	
	protected Logger log = Logger.getLogger(this.getClass());

	private BackupJobDao getDao() {
		return dal.createBackupJobDao();
	}

	@Override
	public BackupJob createBackupJob(BackMeUpUser user,
			Set<ProfileOptions> sourceProfiles, Profile sinkProfile,
			List<ActionProfile> requiredActions, Date start, long delayInMs,
			String keyRing, String jobTitle) {	    
	    try {
  	    conn.begin();
  	    UserDao ud = dal.createUserDao();
  	    user = ud.merge(user);
  	    // Create BackupJob entity in DB...
        BackupJob job = new BackupJob(
            user,
            sourceProfiles,
            sinkProfile,
                requiredActions, 
                start, delayInMs, jobTitle);
        
        Long firstExecutionDate = start.getTime() + delayInMs;
        
        // reusable=true means, that we can get the data for the token + a new token for the next backup
        Token t = keyserver.getToken(job, keyRing, firstExecutionDate, true);
        job.setToken(t);
  	    job = getDao().save(job);
  	    conn.commit();
  	    // ... and queue immediately
        queueJob(job);
        return job;
	    } finally {
	      conn.rollback();
	    }	    
	    
	}
	
	@Override
	public BackupJob getBackUpJob(Long jobId) {
	  return getDao().findById(jobId);  	  	
	}

	@Override
	public void start() {
      // TODO only take N next recent ones (at least if allJobs has an excessive length)
	  try {
	    conn.begin();
  		for (BackupJob storedJob : getDao().findAll()) {
  			queueJob(storedJob);
  		}
	  } finally {
	    conn.rollback();
	  }
	}

	@Override
	public void shutdown() {
		// Shutdown system component
	  system.shutdown();
	  system.awaitTermination();	  
	}
	
	abstract protected Runnable newJobRunner(final BackupJob job);
	
	private void queueJob(BackupJob job) {
		try {		    
			// Compute next job execution time
		  long currentTime = new Date().getTime();
			long executeIn = job.getStart().getTime() - currentTime;
			
			// If job execution was scheduled for within the past 5 mins, still schedule now...
			if (executeIn >= -300000 && executeIn < 0)
				executeIn = 0;
			
			// ...otherwise, schedule on the next occasion defined by .getStart and .getDelay
			if (executeIn < 0) {
				executeIn += Math.ceil((double) Math.abs(executeIn) / (double) job.getDelay()) * job.getDelay();

				// TODO we need to update these jobs' tokens - but where do we get keyRing password from?
			    job.getToken().setBackupdate(currentTime + executeIn);
			      
			    // get access data + new token for next access
			    AuthDataResult authenticationData = keyserver.getData(job.getToken());
			      
			    // the token for the next getData call
			    Token newToken = authenticationData.getNewToken();
			    job.setToken(newToken);
			}
	    
			// TODO we can use the 'cancellable' to terminate later on
			Cancellable cancellable = system.scheduler().schedule(
				Duration.create(executeIn, TimeUnit.MILLISECONDS), // Initial delay
				Duration.create(job.getDelay(), TimeUnit.MILLISECONDS), // Interval
				newJobRunner(job));
		} catch (Exception e) {
			// TODO there must be error handling defined in the JobManager!^
		  Logger.getLogger(AkkaJobManager.class).error("Error during startup", e);
			//throw new BackMeUpException(e);
		}		
	}

}
