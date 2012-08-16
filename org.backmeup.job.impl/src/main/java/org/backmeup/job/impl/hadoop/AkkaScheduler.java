package org.backmeup.job.impl.hadoop;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.backmeup.dal.DataAccessLayer;
import org.backmeup.job.JobManager;
import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.quartz.CronExpression;

import akka.actor.ActorSystem;
import akka.util.Duration;

@ApplicationScoped
public class AkkaScheduler implements JobManager {
	
	private static final ActorSystem system = ActorSystem.create();
	
	@Inject
	private DataAccessLayer dal;
	
	@Override
	public BackupJob createBackupJob(User user,
			Set<ProfileOptions> sourceProfiles, Profile sinkProfile,
			Set<ActionProfile> requiredActions, String timeExpression,
			String keyRing) {
		
	    BackupJob job = new BackupJob(
	    		user,
	    		sourceProfiles,
	    		sinkProfile,
	            requiredActions, 
	            timeExpression);
	    
	    job = dal.createBackupJobDao().save(job);
		
		try {
		    Date now = new Date();
			long executeIn = new CronExpression(timeExpression).getNextValidTimeAfter(now).getTime() - now.getTime();
	    
			system.scheduler().scheduleOnce(
				Duration.create(executeIn, TimeUnit.MILLISECONDS), 
				new Runnable() {
					@Override
					public void run() {
						// TODO submit the Job to Hadoop
					}
				});
		
			return job;
		} catch (ParseException e) {
			// TODO there must be error handling defined in the JobManager!
			throw new RuntimeException(e);
		}
	}

	@Override
	public BackupJob getBackUpJob(Long jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
