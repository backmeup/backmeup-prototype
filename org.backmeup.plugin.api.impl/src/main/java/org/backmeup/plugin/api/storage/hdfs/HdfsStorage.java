package org.backmeup.plugin.api.storage.hdfs;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.hadoop.fs.FileSystem;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

/**
 * TODO implement!
 */
public class HdfsStorage extends Storage {
	
	public HdfsStorage(FileSystem filesystem) {
		
	}

	@Override
	public void open(String path) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getDataObjectCount() throws StorageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<DataObject> getDataObjects() throws StorageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean existsPath(String path) throws StorageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addFile(InputStream is, String path, MetainfoContainer metadata)
			throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeFile(String path) throws StorageException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeDir(String path) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move(String fromPath, String toPath) throws StorageException {
		// TODO Auto-generated method stub
		
	}
}
