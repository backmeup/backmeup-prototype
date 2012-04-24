package org.backmeup.plugin.api.connectors;

import java.util.Properties;

import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;

public interface Datasink {
	
	public abstract String upload(Properties accessData, StorageReader storage, Progressable progressor)
		throws DatasinkException, StorageException;

}
