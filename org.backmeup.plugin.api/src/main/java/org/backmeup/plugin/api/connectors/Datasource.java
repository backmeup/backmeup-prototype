package org.backmeup.plugin.api.connectors;

import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * An abstract base class for all datasource implementations
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public interface Datasource {
	/**
	 * Downloads the entire content of this datasource to the provided
	 * data storage.
	 * @param storage the datastorage
	 * @return a DataObject handle to access the download in the store (e.g. a root directory etc.)
	 */
	public abstract void downloadAll(Properties accessData, List<String> options, Storage storage, Progressable progressor) throws DatasourceException, StorageException;
	
	/**
	 * Returns any type of overview information/statistics that can be
	 * shown to the user as 'detail view' for this datasource. The
	 * returned string can be HTML formatted.
	 * 
	 * @return statistics or any other information about the datasource
	 */
	public abstract String getStatistics(Properties accesssData);
	
	/**
	 * Returns a List of items that might be chosen for the backup.
	 * e.g. a social media plugin could return: "Pictures, Videos, Messages",
	 *      a filestorage plugin could return the root folders of an account.
	 * @return
	 */
	public List<String> getAvailableOptions(Properties accessData);

}
