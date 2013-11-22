package org.backmeup.job;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.backmeup.model.ActionProfile;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;

/**
 * 
 * The JobManager is the interface to 
 * create new jobs which will then be 
 * run asynchronously by this layer.
 * 
 * A JobManager may start up a framework
 * to run all queued backup jobs.
 * 
 * @author fschoeppl
 *
 */
public interface JobManager {

	public BackupJob createBackupJob(BackMeUpUser user,
			Set<ProfileOptions> sourceProfiles, Profile sinkProfile,
			List<ActionProfile> requiredActions, Date start, long delay, String keyRing, String jobTitle,
			boolean reschedule, String timeExpression);

	
	public BackupJob getBackUpJob(Long jobId);
	public void runBackUpJob(BackupJob job);
	
	public void start();
	public void shutdown();
}
