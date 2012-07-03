package org.backmeup.skydrive.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageWriter;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;
import org.backmeup.skydrive.SkyDriveDatasource;


public class SkyDriveDatasourceTest {
	public static void main(String[] args) throws IOException, StorageException {
		// Use the properties saved during SkyDriveAuthenticate to download all files from SkyDrive
		Properties props = new Properties();
    props.load(new FileInputStream(new File("auth.props")));    
		
		SkyDriveDatasource source = new SkyDriveDatasource();
		StorageWriter sw = new LocalFilesystemStorageWriter();
		sw.open("C:/TEMP/TEST/");
		source.downloadAll(props, sw, new Progressable() {
			@Override
			public void progress(String message) {}
		});
	}
}
