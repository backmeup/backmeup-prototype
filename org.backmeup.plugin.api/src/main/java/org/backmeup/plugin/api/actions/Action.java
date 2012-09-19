package org.backmeup.plugin.api.actions;

import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;

public interface Action {

	public void doAction(Properties parameters, StorageReader input, StorageWriter output, BackupJob job, Progressable progressor)
			throws ActionException;
	
}
