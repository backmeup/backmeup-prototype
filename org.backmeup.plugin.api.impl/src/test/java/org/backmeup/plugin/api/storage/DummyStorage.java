package org.backmeup.plugin.api.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.filesystem.FileDataObject;

public class DummyStorage extends Storage {
	
	private List<DataObject> dataObjects = new ArrayList<DataObject>();
	
	public DummyStorage() {
		dataObjects.add(new FileDataObject("src/test/resources/creative-commons.jpg"));
		dataObjects.add(new FileDataObject("src/test/resources/creative-commons.png"));
		dataObjects.add(new FileDataObject("src/test/resources/creative-commons.pdf"));
	}

	@Override
	public void open(String path) throws StorageException {
		// Do nothing - this is just a dummy	
	}

	@Override
	public Iterator<DataObject> getDataObjects() throws StorageException {
		return dataObjects.iterator();
	}

	@Override
	public void close() throws StorageException {
		// Do nothing - this is just a dummy
	}

	@Override
	public int getDataObjectCount() throws StorageException {
	return 3;
	}
	
	@Override
	public boolean existsPath(String path) throws StorageException {
		// Just a dummy
		return false;
	}

	@Override
	public void delete() throws StorageException {
		// TODO Auto-generated method stub
		
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

  @Override
  public long getDataObjectSize() throws StorageException {
    // TODO Auto-generated method stub
    return 500l;
  }

}
