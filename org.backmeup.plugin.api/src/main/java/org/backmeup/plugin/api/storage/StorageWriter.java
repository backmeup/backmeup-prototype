package org.backmeup.plugin.api.storage;

import java.io.InputStream;

import org.backmeup.plugin.api.MetainfoContainer;

/**
 * @deprecated  Use {@link Storage} instead
 */
@Deprecated
public abstract class StorageWriter {
	
	private static Class<? extends StorageWriter> clazz = null;
	
	public static StorageWriter configuredRuntimeInstance() {
		/*try {
			Properties p = new Properties();
			InputStream is = StorageReader.class.getResourceAsStream("conf/application.conf");			
			p.load(is);
			is.close();
			if (clazz == null)
				clazz = 
					Class.forName(p.getProperty("backmeup.storage.reader.impl") + 
							"Writer")
					.asSubclass(StorageWriter.class);			
			
			
			return clazz.newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}*/
		try {
			if (clazz == null)
				clazz = Class.forName("org.backmeup.api.storage.filesystem.LocalFilesystemStorageWriter").asSubclass(StorageWriter.class);
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public abstract void open(String path) throws StorageException;
	
	public abstract void addFile(InputStream is, String path) throws StorageException;
	
	public abstract void addFile(InputStream is, String path, MetainfoContainer metadata) throws StorageException;
			
	public abstract void close() throws StorageException;
	
}
