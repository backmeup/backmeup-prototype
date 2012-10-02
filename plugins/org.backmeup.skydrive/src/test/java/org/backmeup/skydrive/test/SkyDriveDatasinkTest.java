package org.backmeup.skydrive.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
import org.backmeup.skydrive.SkyDriveDatasink;

public class SkyDriveDatasinkTest {
	public static void main(String args[]) throws FileNotFoundException, IOException, StorageException {
		// use the property file saved in SkyDriveAuthenticate to store all data from "C:/TEMP/TEST"
		Properties props = new Properties();
    props.load(new FileInputStream(new File("auth.props")));    
		
		SkyDriveDatasink sink = new SkyDriveDatasink();
		Storage sr = new LocalFilesystemStorage();
		sr.open("C:/TEMP/TEST/");
		
		sink.upload(props, sr, new Progressable(){
			@Override
			public void progress(String message) {}
		});
	}
}
