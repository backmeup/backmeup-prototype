package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.AfterClass;
import org.junit.Test;

public class LocalFilesystemIOTest {
	
	  @Test
	  public void testLocalFilesystemStorageWriter() throws FileNotFoundException, StorageException {
		  // If the writer writes without throwing exceptions, we're ok
		  LocalFilesystemStorageWriter writer = new LocalFilesystemStorageWriter();
		  writer.open("target/test-storage/filesystem");
		  writer.addFile(new FileInputStream(new File("src/test/resources/800px-Hallstatt_300.jpg")), "images/test.jpg");
		  writer.close();
	  }
	  
	  @Test
	  public void testLocalFilesystemStorageReader() throws StorageException, IOException {
		  // Now let's check what we've written before
		  LocalFilesystemStorageReader reader = new LocalFilesystemStorageReader();
		  reader.open("target/test-storage/filesystem");
		  Iterator<DataObject> it = reader.getDataObjects();
		  assert(it.hasNext());
		  while (it.hasNext()) {
			  DataObject dataobject = it.next();
			  assert(dataobject.getPath().equals("/filesystem/images/test.jpg"));
			  assert(dataobject.getBytes().length == 212919);
		  }
	  }
	  
	  @AfterClass
	  public static void delete() throws IOException {
		  FileUtils.deleteDirectory(new File("target/test-storage"));
	  }
	
}
