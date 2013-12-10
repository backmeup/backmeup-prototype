package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class LocalFilesystemStorageReader extends StorageReader {
	private final Logger logger = LoggerFactory.getLogger(LocalFilesystemStorageReader.class);

	private File directory;
	
	@Override
	public void open(String path) {
		this.directory = new File(path);
		if (!this.directory.exists())
			this.directory.mkdir();
	}

	@Override
	public Iterator<DataObject> getDataObjects() throws StorageException {				
		final List<DataObject> flatList = new ArrayList<DataObject>(); 
		addToList(directory, "/", flatList);
		
		return new Iterator<DataObject>() {
			
			private int idx = 0;
			
			public boolean hasNext() {
				return idx < flatList.size();
			}

			public DataObject next() {
				DataObject obj = flatList.get(idx);
				logger.info("Retrieving from Storage: " + obj.getPath());
				idx++;
				return obj;
			}

			public void remove() {
				// Do nothing				
			}
			
		};
	}
	
	private void addToList(File file, String path, List<DataObject> objects) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				addToList(child, path + "/" + file.getName(), objects);
			}
		} else {
		  // don't add meta.json files!
		  if (file.getName().endsWith("meta.json"))
        return;
		  
		  if (path.startsWith("/")||path.startsWith("\\"))
				path = path.substring(1);
			
			objects.add(new FileDataObject(file, path + "/" + file.getName()));
		}
	}
	
	@Override
	public boolean existsPath(String path) throws StorageException {
		if (path.startsWith("/")||path.startsWith("\\"))
			path = path.substring(1);
		
		return new File(directory, path).exists();
	}

	@Override
	public void close() throws StorageException {
		// TODO Auto-generated method stub
		
	}

  @Override
  public int getDataObjectCount() throws StorageException {
    final List<DataObject> flatList = new ArrayList<DataObject>(); 
    addToList(directory, "/", flatList);
    return flatList.size();
  }

}
