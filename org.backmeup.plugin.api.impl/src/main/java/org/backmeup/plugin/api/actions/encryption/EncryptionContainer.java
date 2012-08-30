package org.backmeup.plugin.api.actions.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.storage.DataObject;

public class EncryptionContainer
{
	private String container_path;
	private String container_name;
	private String mountpoint;
	private String password;
	private long size;
	private List<DataObject> data;

	// TODO Remove this constructor
	public EncryptionContainer ()
	{
	}
	
	public EncryptionContainer (String container_name,String container_path, String mountpoint, String password, long size)
	{
		this.container_name = container_name;
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
		File f = new File ("mountpoint");
		if (f.exists () == false)
		{
			f.mkdir ();
		}
		else
		{
			// TODO throw something
		}
		
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
	
	public InputStream getContainer () throws FileNotFoundException
	{
		FileInputStream is = new FileInputStream (container_path);
		
		return is;
	}
	
	public void deleteContainer ()
	{
		File file = new File (container_path + container_name);
		file.delete ();
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
