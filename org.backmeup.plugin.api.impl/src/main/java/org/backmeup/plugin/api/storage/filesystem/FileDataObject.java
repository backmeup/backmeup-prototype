package org.backmeup.plugin.api.storage.filesystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.backmeup.plugin.api.storage.DataObject;

public class FileDataObject implements DataObject {
	
	private File file;
	
	private String path;
	
	public FileDataObject(File file, String path) {
		this.file = file;
		this.path = path;
	}

	public byte[] getBytes() throws IOException {
	  // Replaced IOUtils because they returned too many bytes (and I don't know why).
	  InputStream is = new FileInputStream(file);
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  try {
  	  int maxLen = 1024 * 1024;
  	  byte[] buffer = new byte[maxLen];
  	  int len;
  	  while((len = is.read(buffer, 0, maxLen)) > 0) {
  	    baos.write(buffer, 0, len);	    
  	  }
  	  return baos.toByteArray();
	  } finally {
	    baos.close();
	    is.close();
	  }
	}

	public String getPath() {
		return path;
	}

	public String getProperty(String name) {
		// Metadata properties not supported on filesystem
		return null;
	}
	
	@Override
	public String toString() {
		try {
			return path + " (" + getBytes().length + " bytes)";
		} catch (IOException e) {
			return path + " (corrupt file?)";
		}
	}

	public long getLength() {
		return file.length();
	}

}
