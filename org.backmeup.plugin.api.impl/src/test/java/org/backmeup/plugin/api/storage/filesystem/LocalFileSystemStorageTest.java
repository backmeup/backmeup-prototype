package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.tools.ant.filters.StringInputStream;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;
import org.junit.Test;

public class LocalFileSystemStorageTest {
	
	private static final String ROOT_PATH = "unit-test";
	
	private static final String TEST_TXT_1 = "Hello World! #1";
	
	private static final String TEST_TXT_2 = "Hello World! #2";
	
	@Test
	public void testOpen() throws StorageException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		
		// Directory should exist after open
		File rootPath = new File(ROOT_PATH);	
		Assert.assertTrue(rootPath.exists());
		Assert.assertTrue(rootPath.isDirectory());
	}
	
	@Test
	public void testAddFile() throws StorageException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		
		// Add two test files
		storage.addFile(new StringInputStream(TEST_TXT_1), "/hello1.txt", new MetainfoContainer());
		storage.addFile(new StringInputStream(TEST_TXT_2), "/mydirectory/hello2.txt", new MetainfoContainer());
		
		File rootPath = new File(ROOT_PATH);	
		File file1 = new File(rootPath, "hello1.txt");
		File meta1 = new File(rootPath, "hello1.txt.meta.json");
		File file2 = new File(new File(rootPath, "mydirectory"), "hello2.txt");
		File meta2 = new File(new File(rootPath, "mydirectory"), "hello2.txt.meta.json");
		
		// Both files and meta json should exist on the file system
		Assert.assertTrue(file1.exists());
		Assert.assertTrue(file1.isFile());
		Assert.assertTrue(meta1.exists());
		Assert.assertTrue(meta1.isFile());
		Assert.assertTrue(file2.exists());
		Assert.assertTrue(file2.isFile());
		Assert.assertTrue(meta2.exists());
		Assert.assertTrue(meta2.isFile());
	}
	
	@Test
	public void testGetDataObjectCount() throws StorageException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		
		// Storage should contain two files
		Assert.assertEquals(2, storage.getDataObjectCount());		
	}
	
	@Test
	public void testGetDataObjects() throws StorageException, IOException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		
		List<DataObject> dataobjects = new ArrayList<DataObject>();
		Iterator<DataObject> it = storage.getDataObjects();
		while (it.hasNext()) 
			dataobjects.add(it.next());
		
		// Storage should return two data objects
		Assert.assertEquals(2, dataobjects.size());
		
		// Data Objects should be equal to test texts
		List<String> expected = new ArrayList<String>();
		expected.add(TEST_TXT_1);
		expected.add(TEST_TXT_2);
		
		String actual1 = new String(dataobjects.get(0).getBytes());
		String actual2 = new String(dataobjects.get(1).getBytes());
		
		Assert.assertTrue(expected.contains(actual1));
		Assert.assertTrue(expected.contains(actual2));
		Assert.assertFalse(actual1.equals(actual2));	
	}
	
	@Test
	public void testMoveFile() throws StorageException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		
		// Trying to move a directory should throw a StorageException
		try {
			storage.moveFile("/mydirectory/", "/my-new-directory/");
			Assert.fail();
		} catch (StorageException e) {
			Assert.assertTrue(e.getMessage().contains("directories"));
		}
		
		// Move file
		storage.moveFile("/mydirectory/hello2.txt", "/my-new-directory/hello3.txt");
		
		File dir = new File(ROOT_PATH, "my-new-directory");
		File file = new File(dir, "hello3.txt");
		File meta = new File(dir, "hello3.txt.meta.json");
		Assert.assertTrue(file.exists());
		Assert.assertTrue(file.isFile());
		Assert.assertTrue(meta.exists());
		Assert.assertTrue(meta.isFile());
	}
	
	@Test
	public void testRemoveFile() throws StorageException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		
		File file = new File(new File(ROOT_PATH, "my-new-directory"), "hello3.txt");
		File meta = new File(new File(ROOT_PATH, "my-new-directory"), "hello3.txt.meta.json");
		Assert.assertTrue(file.exists());
		Assert.assertTrue(meta.exists());
		
		storage.removeFile("/my-new-directory/hello3.txt");
		
		Assert.assertFalse(file.exists());
		Assert.assertFalse(meta.exists());
	}
	
	@Test
	public void testDelete() throws StorageException {
		Storage storage = new LocalFilesystemStorage();
		storage.open(ROOT_PATH);
		storage.delete();
		
		File rootPath = new File(ROOT_PATH);
		Assert.assertFalse(rootPath.exists());
	}

}
