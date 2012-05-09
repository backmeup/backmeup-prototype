package org.backmeup.plugin.api.connectors;

import java.util.Properties;

import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;

/**
 * 
 * The Datasink interface is the contract
 * for an upload of files to a certain datasink.
 * 
 * 
 * @author fschoeppl
 *
 */
public interface Datasink {
	
	public String upload(Properties accessData, StorageReader storage, Progressable progressor)
		throws StorageException;

}
