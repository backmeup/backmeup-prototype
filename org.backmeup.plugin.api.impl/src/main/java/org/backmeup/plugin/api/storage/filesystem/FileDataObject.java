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
		
		String[] parts = path.split ("/");
		this.path = "";
		// Ignore the first folder. parts[0] = "", parts[1] = "job-xxxxx"
		for (int i = 2; i < parts.length; i++)
		{
			this.path += "/" + parts[i];
		}
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

	@Override
	public void setMetainfo(MetainfoContainer meta) {
		try {
			String json = MetainfoContainer.toJSON(meta);
			FileUtils.writeStringToFile(metaFile, json, false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}