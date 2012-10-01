package org.backmeup.discmailing.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.backmeup.discmailing.DiscmailingDatasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.junit.Test;

public class DiscmailingDatasinkTest {
	
	@Test
	public void testDiscmailing() {
	    // fail("Not yet implemented");
	}
	
	/**
	 * @param args
	 */
	public static void main(String args[]) throws FileNotFoundException, IOException, StorageException {
		System.out.println("DiscDatasinkTest");
		Properties props = new Properties();
	    props.load(new FileInputStream(new File("/tmp/auth.props")));    

	    DiscmailingDatasink sink = new DiscmailingDatasink();
		StorageReader sr = new LocalFilesystemStorageReader();
		sr.open("/data/backmeup/users/1/");

		sink.upload(props, sr, new Progressable(){
			@Override
			public void progress(String message) {}
		});	
	}	
}
