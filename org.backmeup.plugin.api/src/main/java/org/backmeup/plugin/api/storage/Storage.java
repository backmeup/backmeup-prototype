package org.backmeup.plugin.api.storage;

import java.io.InputStream;
import java.util.Iterator;

import org.backmeup.plugin.api.MetainfoContainer;

public abstract class Storage {
	
	public abstract void open(String path) throws StorageException;
	
	public abstract void close() throws StorageException;	
	
	public abstract void delete() throws StorageException;
	
	/** Read methods **/
	
	public abstract int getDataObjectCount() throws StorageException;
	
	// The total size of all objects within this storage in bytes
	public abstract long getDataObjectSize() throws StorageException;
	
	public abstract Iterator<DataObject> getDataObjects() throws StorageException;
	
	public abstract boolean existsPath(String path) throws StorageException;
	
	/** Write methods **/
	
	public abstract void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException;
	
	public abstract void removeFile(String path) throws StorageException;
	
	public abstract void removeDir(String path) throws StorageException;
	
	public abstract void move(String fromPath, String toPath) throws StorageException;
	
}
