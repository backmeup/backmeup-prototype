package org.backmeup.plugin.api.connectors;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;

/**
 * An abstract base class for datasources following a filesystem-like paradigm.
 * These datasources are arranged in a hierarchical structure of folders and files. 
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public abstract class FilesystemLikeDatasource implements Datasource {
	
	
	public void downloadAll(Properties accessData, StorageWriter storage, Progressable progressor) throws StorageException {
		List<FilesystemURI> files = list(accessData);
		for (int i=0; i < files.size(); i++) {
			FilesystemURI uri = files.get(i);			
			download(accessData, uri, storage, progressor);			
		}
	}
	
	private void download(Properties accessData, FilesystemURI uri, StorageWriter storage, Progressable progressor) throws StorageException {
		if (uri.isDirectory()) {
			//Logger.info("Downloading contents of directory " + uri);
			for (FilesystemURI child : list(accessData, uri)) {
				download(accessData, child, storage, progressor);
			}
		} else {
			//Logger.info("Downloading file " + uri);
			progressor.progress(String.format("Downloading file %s ...", uri.toString()));
			InputStream is = getFile(accessData, uri);
			if (is == null) {
				//Logger.warn("Got a null input stream for " + uri.getUri().getPath().toString());
			} else {
			  URI destination = uri.getMappedUri();
			  if (destination == null)
			    destination = uri.getUri();
				storage.addFile(is, destination.getPath().toString());
			}
		}
	}
	
	public List<FilesystemURI> list(Properties accessData) {
		return list(accessData, null);
	}
	
	public abstract List<FilesystemURI> list(Properties accessData, FilesystemURI uri);
	
	public abstract InputStream getFile(Properties accessData, FilesystemURI uri);
	
}
