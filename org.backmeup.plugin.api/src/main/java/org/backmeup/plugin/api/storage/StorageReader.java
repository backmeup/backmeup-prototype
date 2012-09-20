package org.backmeup.plugin.api.storage;

import java.util.Iterator;

/**
 * @deprecated  Use {@link Storage} instead
 */
@Deprecated
public abstract class StorageReader {
	
	private static Class<? extends StorageReader> clazz = null;
	
	public static StorageReader configuredRuntimeInstance() {
		/*try {
			Properties p = new Properties();
			InputStream is = StorageReader.class.getResourceAsStream("conf/application.conf");
			p.load(is);
			is.close();
			
			if (clazz == null)
				clazz = 
					Class.forName( p.getProperty("backmeup.storage.reader.impl") + 
							"Reader")
					.asSubclass(StorageReader.class);			
			
			return clazz.newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}*/
		
		try {
			if (clazz == null)
				clazz = Class.forName("org.backmeup.api.storage.filesystem.LocalFilesystemStorageReader").asSubclass(StorageReader.class);
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public abstract int getDataObjectCount() throws StorageException;
	
	public abstract void open(String path) throws StorageException;
	
	public abstract Iterator<DataObject> getDataObjects() throws StorageException;
	
	public abstract boolean existsPath(String path) throws StorageException;
			
	public abstract void close() throws StorageException;
}
