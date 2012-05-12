package org.backmeup.plugin.api.actions;

import java.util.Properties;

import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;

public interface Action {

	public String doAction(Properties accessData, StorageReader storage, Progressable progressor)
			throws ActionException;
	
}
