package org.backmeup.dropbox.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.backmeup.dropbox.DropboxDatasource;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;


public class DropboxDatasourceTest {
	public static void main(String[] args) throws IOException, StorageException {
		// Use the properties saved during DropboxAuthenticate to download all files from Dropbox
		Properties props = new Properties();
    props.load(new FileInputStream(new File("auth.props")));    
		
		DropboxDatasource source = new DropboxDatasource();
		StorageWriter sw = new LocalFilesystemStorageWriter();
		sw.open("C:/TEMP/TEST/");
		source.downloadAll(props, sw, new Progressable() {
			@Override
			public void progress(String message) {}
		});
		
		StorageReader sr = new LocalFilesystemStorageReader();
		sr.open("C:/TEMP/TEST/");
		Iterator<DataObject> it = sr.getDataObjects();
		while (it.hasNext()) {
			DataObject da = it.next();
			System.out.println(da.getMetainfo());
			System.out.println();
		}
	}
}
