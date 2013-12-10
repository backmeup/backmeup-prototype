package org.backmeup.plugin.api.actions.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.storage.DataObject;

public class EncryptionContainer
{	
	private String containerpath;
	private String containername;
	private String mountpoint;
	private String password;
	private long size;
	private List<DataObject> data;
	
	public EncryptionContainer (String containername, String containerpath, String mountpoint, String password, long size)
	{	
		this.containername = containername;
		this.containerpath = containerpath;
		this.mountpoint = mountpoint;
		this.password = password;
		this.size = size;
		this.data = new LinkedList<DataObject> ();
	}

	private void writeData () throws IOException
	{
		for (DataObject daob : data)
		{
			String[] parts = daob.getPath ().split ("/");
			String fspath = mountpoint;
			for (int i = 2; i < parts.length; i++)
			{
				fspath += "/" + parts[i];
			}
			
			String dirpath = mountpoint;
			for (int i = 2; i < parts.length - 1; i++)
			{
				dirpath += "/" + parts[i];
			}
			
			//System.out.println ("Write file to container: " + fspath);
			
			File dirs = new File (dirpath);
			dirs.mkdirs ();
			
			FileOutputStream fo = new FileOutputStream (fspath);
			fo.write (daob.getBytes ());
			fo.flush ();
			fo.close ();
		}
	}

	private void createContainer () throws ActionException
	{
		File f = new File (mountpoint);
		if (f.exists () == false)
		{
			if (f.mkdirs () == false)
			{
				// TODO throw CantCreateFolder...
				throw new ActionException ("Can't create mountpint: " + mountpoint);
			}
		}
		else
		{
			// TODO throw FolderAlready...
			throw new ActionException ("Mountpoint already exists: " + mountpoint);
		}
		
		EncryptionTcManager tcmanager = new EncryptionTcManager ();
		tcmanager.createContainer (this);
	}

	private void unmountContainer () throws ActionException
	{
		EncryptionTcManager tcmanager = new EncryptionTcManager ();
		tcmanager.unmountContainer (this);
	}

	public void writeContainer () throws IOException, ActionException
	{
		createContainer ();
		writeData ();
		unmountContainer ();
	}
	
	public InputStream getContainer () throws FileNotFoundException
	{
		FileInputStream is = new FileInputStream (containerpath);
		
		return is;
	}
	
	public void deleteContainer ()
	{
		File file = new File (containerpath);
		file.delete ();
	}

	public void addData (DataObject data)
	{
		this.data.add (data);
	}
	
	public List<DataObject> getData ()
	{
		return data;
	}

	public String getContainername ()
	{
		return containername;
	}

	public String getContainerpath ()
	{
		return containerpath;
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
