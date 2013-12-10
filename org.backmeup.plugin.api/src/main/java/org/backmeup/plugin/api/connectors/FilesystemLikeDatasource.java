package org.backmeup.plugin.api.connectors;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class for datasources following a filesystem-like paradigm.
 * These datasources are arranged in a hierarchical structure of folders and files. 
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public abstract class FilesystemLikeDatasource implements Datasource { 
	
	private final Logger logger = LoggerFactory.getLogger(FilesystemLikeDatasource.class);
	
	public void downloadAll(Properties accessData, List<String> options, Storage storage, Progressable progressor) throws StorageException {
		List<FilesystemURI> files = list(accessData, options);
		for (int i=0; i < files.size(); i++) {
			FilesystemURI uri = files.get(i);			
			download(accessData, options, uri, storage, progressor);			
		}
	}
	
	private void download(Properties accessData, List<String> options, FilesystemURI uri, Storage storage, Progressable progressor) throws StorageException {
	  MetainfoContainer metainfo = uri.getMetainfoContainer();	  
		if (uri.isDirectory()) {
			logger.info("Downloading contents of directory " + uri);
			for (FilesystemURI child : list(accessData, options, uri)) {
				download(accessData, options, child, storage, progressor);
			}
		} else {
			logger.info("Downloading file " + uri);
			progressor.progress(String.format("Downloading file %s ...", uri.toString()));
			InputStream is = getFile(accessData, options, uri);
			if (is == null) {
				logger.warn("Got a null input stream for " + uri.getUri().getPath().toString());
				progressor.progress(String.format("Downloading file %s failed!", uri.toString()));
			} else {
			  URI destination = uri.getMappedUri();
			  if (destination == null)
			    destination = uri.getUri();
				storage.addFile(is, destination.getPath().toString(), metainfo);
			}
		}
	}
	
	public List<FilesystemURI> list(Properties accessData, List<String> options) {
		return list(accessData, options, null);
	}
	
	public abstract List<FilesystemURI> list(Properties accessData, List<String> options, FilesystemURI uri);
	
	public abstract InputStream getFile(Properties accessData, List<String> options, FilesystemURI uri);
	
}
