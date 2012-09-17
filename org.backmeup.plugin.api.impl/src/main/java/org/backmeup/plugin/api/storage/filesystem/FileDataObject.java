package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;

public class FileDataObject implements DataObject {

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
	  FileReader fr = null;
	  try {
  	  fr = new FileReader(file);
  		return IOUtils.toByteArray(fr);
	  } finally {
	    fr.close();
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