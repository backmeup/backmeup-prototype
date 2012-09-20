package org.backmeup.plugin.api.storage;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.backmeup.plugin.api.storage.filesystem.FileDataObject;

public class DummyStorageReader extends StorageReader {
	
	private List<DataObject> dataObjects = new ArrayList<DataObject>();
	
	public DummyStorageReader() {
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

}
