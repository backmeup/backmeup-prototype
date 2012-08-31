package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

public class LocalFilesystemStorage extends Storage {
	
	private File rootDir;

	@Override
	public void open(String rootPath) throws StorageException {
		this.rootDir = new File(rootPath);
		if (!this.rootDir.exists()) 
			this.rootDir.mkdirs();
	}

	@Override
	public void close() throws StorageException {
		// Do nothing
	}
	
	@Override
	public void delete() throws StorageException {
		try {
			FileUtils.deleteDirectory(rootDir);
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}

	/** Read methods **/
	
	@Override
	public int getDataObjectCount() throws StorageException {
	    final List<DataObject> flatList = new ArrayList<DataObject>(); 
	    addToList(rootDir, "/", flatList);
	    return flatList.size();
	}

	@Override
	public Iterator<DataObject> getDataObjects() throws StorageException {
		final List<DataObject> flatList = new ArrayList<DataObject>(); 
		addToList(rootDir, "/", flatList);
		
		return new Iterator<DataObject>() {
			
			private int idx = 0;
			
			public boolean hasNext() {
				return idx < flatList.size();
			}

			public DataObject next() {
				DataObject obj = flatList.get(idx);
				//Logger.info("Retrieving from Storage: " + obj.getPath());
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
	
	/** Write methods **/

	@Override
	public void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException {
		try {
			File out = new File(rootDir, path);
			out.getParentFile().mkdirs();
				
			OutputStream os = new FileOutputStream(out);
							
			byte buf[] = new byte[1024 * 1024];
			int len;
			while((len = is.read(buf)) > 0)
				os.write(buf, 0, len);
				
			os.close();
			is.close();
			if (metadata != null) {
			  byte[] json = getGson().toJson(metadata).getBytes("UTF-8");
			  File metaFile = new File(rootDir, path + ".meta.json");
			  os = new FileOutputStream(metaFile);
			  os.write(json, 0, json.length);
			  os.close();
			}
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public void moveFile(String fromPath, String toPath) throws StorageException {	
		try {
			File from = new File(rootDir, fromPath);
			File to = new File(rootDir, toPath);
			
			if (from.isDirectory() || to.isDirectory())
				throw new StorageException("Cannot move directories");
				
			if (!from.exists())
				throw new StorageException("Cannot move " + fromPath + " - does not exist");
			
			if (to.exists())
				throw new StorageException("Cannot move to " + toPath + " - already exists");
				
 			FileUtils.moveFile(from, to);
 			
 			File fromMeta = new File(rootDir, fromPath + ".meta.json");
 			if (fromMeta.exists())
 				FileUtils.moveFile(fromMeta, new File(rootDir, toPath + ".meta.json"));
		} catch (IOException e) {
			throw new StorageException(e);
		} 
	}

	@Override
	public void removeFile(String path) throws StorageException {
		File file = new File(rootDir, path);
		if (!file.exists())
			throw new StorageException("Cannot remove " + path + " - does not exist");
		
		file.delete();
		
		File meta = new File(rootDir, path + ".meta.json");
		if (meta.exists())
			meta.delete();
	}

}