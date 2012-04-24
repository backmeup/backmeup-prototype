package org.backmeup.plugin.api.connectors;

import java.util.Properties;

import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

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
	public abstract void downloadAll(Properties accessData, StorageWriter storage, Progressable progressor) throws DatasourceException, StorageException;
	
	/**
	 * Returns any type of overview information/statistics that can be
	 * shown to the user as 'detail view' for this datasource. The
	 * returned string can be HTML formatted.
	 * 
	 * @return statistics or any other information about the datasource
	 */
	public abstract String getStatistics(Properties accesssData);

}
