package org.backmeup.plugin.api.storage.hdfs;

import java.io.IOException;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;

public class HdfsDataObject extends DataObject {
	
	private String path;
	private int length;
	private byte[] bytes;

	public HdfsDataObject(String seqPath, String filePath, byte[] barray, int length) {
		// TODO seqPath not needed (verify!)
		this.path = filePath;
		this.bytes = barray;
		this.length = length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		// TODO Auto-generated method stub
		return bytes;
	}
	
	@Override
	public long getLength() {
		return length;
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

@Override
public void setMetainfo(MetainfoContainer meta) {
	// TODO Auto-generated method stub
	
}

}
