package org.backmeup.plugin.api.connectors;

import java.io.InputStream;
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
	
	
	public void downloadAll(Properties items, StorageWriter storage, Progressable progressor) throws StorageException {
		List<FilesystemURI> files = list(items);
		for (int i=0; i < files.size(); i++) {
			FilesystemURI uri = files.get(i);			
			download(items, uri, storage, progressor);			
		}
	}
	
	private void download(Properties items, FilesystemURI uri, StorageWriter storage, Progressable progressor) throws StorageException {
		if (uri.isDirectory()) {
			//Logger.info("Downloading contents of directory " + uri);
			for (FilesystemURI child : list(items, uri)) {
				download(items, child, storage, progressor);
			}
		} else {
			//Logger.info("Downloading file " + uri);
			progressor.progress(String.format("Downloading file %s ...", uri.toString()));
			InputStream is = getFile(items, uri);
			if (is == null) {
				//Logger.warn("Got a null input stream for " + uri.getUri().getPath().toString());
			} else {
				storage.addFile(is, uri.getUri().getPath().toString());
			}
		}
	}
	
	public List<FilesystemURI> list(Properties items) {
		return list(items, null);
	}
	
	public abstract List<FilesystemURI> list(Properties items, FilesystemURI uri);
	
	public abstract InputStream getFile(Properties items, FilesystemURI uri);
	
}
