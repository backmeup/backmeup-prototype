package org.backmeup.plugin.api.actions.indexing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.plugin.api.storage.filesystem.LocalFilesystemStorageWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class StorageIndexerTest {
	
	private static final String STORAGE_PATH = "target/test-storage/filesystem";
	
	@BeforeClass
	public static void setupDummyData() throws FileNotFoundException, StorageException {
		LocalFilesystemStorageWriter writer = new LocalFilesystemStorageWriter();
		writer.open(STORAGE_PATH);
		writer.addFile(new FileInputStream(new File("src/test/resources/800px-Hallstatt_300.jpg")), "images/test.jpg");
		writer.close();		
	}
	
	@Test
	public void testIndexing() throws StorageException, IOException, SAXException, TikaException {
		LocalFilesystemStorageReader reader = new LocalFilesystemStorageReader();
		reader.open(STORAGE_PATH);
		
		StorageIndexer indexer = new StorageIndexer(reader);
		indexer.run();
		
		// TODO implement
	}

}
