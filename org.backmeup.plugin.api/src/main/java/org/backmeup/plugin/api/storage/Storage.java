package org.backmeup.plugin.api.storage;

import java.io.InputStream;
import java.util.Iterator;

import org.backmeup.plugin.api.MetainfoContainer;

public abstract class Storage {

	public abstract void open(String path) throws StorageException;
	
	public abstract void close() throws StorageException;	
	
	/** Read methods **/
	
	public abstract int getDataObjectCount() throws StorageException;
	
	public abstract Iterator<DataObject> getDataObjects() throws StorageException;
	
	/** Write methods **/
	
	public abstract void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException;
	
	public abstract void deleteFile(String path) throws StorageException;
	
	public abstract void moveFile(String fromPath, String toPath) throws StorageException;
	
}
