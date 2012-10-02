package org.backmeup.plugin.api.connectors;

import java.util.Properties;

import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

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
	
	public String upload(Properties accessData, Storage storage, Progressable progressor)
		throws StorageException;

}
