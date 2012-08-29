package org.backmeup.plugin.api.actions.encryption;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.storage.DataObject;

public class EncryptionContainer
{
	private String container_path;
	private String mountpoint;
	private String password;
	private long size;
	private List<DataObject> data;

	public EncryptionContainer (String container_path, String mountpoint, String password, long size)
	{
		this.container_path = container_path;
		this.mountpoint = mountpoint;
		this.password = password;
		this.size = size;
		this.data = new LinkedList<DataObject> ();
	}

	private void writeData () throws IOException
	{
		for (DataObject daob : data)
		{
			FileOutputStream fo = new FileOutputStream (mountpoint + "/" + daob.getPath ());
			fo.write (daob.getBytes ());
			fo.flush ();
			fo.close ();
		}
	}

	private void createContainer ()
	{
		EncryptionTcManager tcmanager = new EncryptionTcManager ();
		tcmanager.createContainer (this);
	}

	private void unmountContainer ()
	{
		EncryptionTcManager tcmanager = new EncryptionTcManager ();
		tcmanager.unmountContainer (this);
	}

	public void writeContainer () throws IOException
	{
		createContainer ();
		writeData ();
		unmountContainer ();
	}

	public void addData (DataObject data)
	{
		this.data.add (data);
	}

	public String getContainer_path ()
	{
		return container_path;
	}

	public String getMountpoint ()
	{
		return mountpoint;
	}

	public String getPassword ()
	{
		return password;
	}

	public long getSize ()
	{
		return size;
	}
}
