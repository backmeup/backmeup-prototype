package org.backmeup.job;

import java.util.List;
import org.backmeup.model.BackupJob;
import org.backmeup.model.Profile;
import org.backmeup.model.ProfileOptions;
import org.backmeup.model.User;
import org.backmeup.model.spi.ActionDescribable;

public interface JobManager {

	public BackupJob createBackupJob(User user,
			List<ProfileOptions> sourceProfiles, Profile sinkProfile,
			List<ActionDescribable> requiredActions, String timeExpression,
			String keyRing);
	
	public void shutdown();
}
