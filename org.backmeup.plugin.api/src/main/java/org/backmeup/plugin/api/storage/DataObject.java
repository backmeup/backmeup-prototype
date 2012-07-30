package org.backmeup.plugin.api.storage;

import java.io.IOException;

import org.backmeup.plugin.api.MetainfoContainer;

public interface DataObject {

	public byte[] getBytes() throws IOException;

	public long getLength();
	
	public String getPath();
	
	public MetainfoContainer getMetainfo();
	
}
