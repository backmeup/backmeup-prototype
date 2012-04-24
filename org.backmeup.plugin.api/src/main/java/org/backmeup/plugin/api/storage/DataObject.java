package org.backmeup.plugin.api.storage;

import java.io.IOException;

public interface DataObject {

	public byte[] getBytes() throws IOException;

	public int getLength();
	
	public String getPath();
	
	public String getProperty(String name);
	
}
