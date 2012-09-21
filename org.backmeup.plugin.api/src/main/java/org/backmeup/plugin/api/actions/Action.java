package org.backmeup.plugin.api.actions;

import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;

public interface Action {

	public void doAction(Properties parameters, Storage storage, BackupJob job, Progressable progressor)
			throws ActionException;
	
}
