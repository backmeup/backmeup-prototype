package org.backmeup.discmailing.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.backmeup.discmailing.DiscmailingDatasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorage;
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
	    Date date = new Date();
	    DateFormat df = new SimpleDateFormat("dd_MM_yy_hh_mm_ss");
	    props.setProperty ("org.backmeup.tmpdir", "BMU_plugin_" +  df.format(date));

	    DiscmailingDatasink sink = new DiscmailingDatasink();
		Storage sr = new LocalFilesystemStorage();
		sr.open("/data/backmeup/users/1/");

		sink.upload(props, sr, new Progressable(){
			@Override
			public void progress(String message) {}
		});	
	}	
}
