package org.backmeup.skydrive.test;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.skydrive.SkyDriveDatasource;


public class SkyDriveDatasourceTest {
	public static void main(String[] args) throws IOException, StorageException {
		// Use the properties saved during SkyDriveAuthenticate to download all files from SkyDrive
		Properties props = new Properties();
    props.load(new FileInputStream(new File("auth.props")));    
		
		SkyDriveDatasource source = new SkyDriveDatasource();
		Storage sw = new LocalFilesystemStorage();
		sw.open("C:/TEMP/TEST/");
		source.downloadAll(props, new ArrayList<String>(), sw, new Progressable() {
			@Override
			public void progress(String message) {}
		});
	}
}
