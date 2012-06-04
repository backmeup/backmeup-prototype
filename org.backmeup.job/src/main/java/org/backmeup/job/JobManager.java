package org.backmeup.job;

import java.util.List;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.backmeup.model.spi.ActionDescribable;

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

	public BackupJob createBackupJob(User user,
			List<ProfileOptions> sourceProfiles, Profile sinkProfile,
			List<ActionDescribable> requiredActions, String timeExpression,
			String keyRing);
	
	public BackupJob getBackUpJob(Long jobId);
	
	public void start();
	public void shutdown();
}
