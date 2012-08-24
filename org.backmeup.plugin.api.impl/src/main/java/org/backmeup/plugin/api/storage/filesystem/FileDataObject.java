package org.backmeup.plugin.api.storage.filesystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;

public class FileDataObject implements DataObject {

	private File file;

	private String path;

	public FileDataObject(String path) {
		this(new File(path), path);
	}
	
	public FileDataObject(File file, String path) {
		this.file = file;
		this.path = path;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return IOUtils.toByteArray(new FileReader(file));
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
	    MetainfoContainer metainfo = new MetainfoContainer();
	    return metainfo;
	}

}