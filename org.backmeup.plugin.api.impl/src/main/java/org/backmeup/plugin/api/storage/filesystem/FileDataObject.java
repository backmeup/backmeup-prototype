package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;

public class FileDataObject extends DataObject {

	private File file;
	
	private File metaFile;

	private String path;

	public FileDataObject(String path) {
		this(new File(path), path);
	}
	
	public FileDataObject(File file, String path) {
		this.file = file;
		this.metaFile = new File(file.getAbsolutePath() + ".meta.json");
		this.path = path;
	}

	@Override
	public byte[] getBytes() throws IOException {
	  FileInputStream fis = null;
	  try {
	    fis = new FileInputStream(file);
  		return IOUtils.toByteArray(fis);
	  } finally {
	    fis.close();
	  }
	}
	
	@Override
	public long getLength() {
		return file.length();
	}
	
	@Override
	public String getPath() {
		return path;
	}

	@Override
	public MetainfoContainer getMetainfo() {
		try {
			String json = FileUtils.readFileToString(metaFile);
			return MetainfoContainer.fromJSON(json);
		} catch (IOException e) {
			// Return empty MetainfoContainer if JSON doesn't exist
			return new MetainfoContainer();
		}
	}

}