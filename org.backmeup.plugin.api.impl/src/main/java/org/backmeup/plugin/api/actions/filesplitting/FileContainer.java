package org.backmeup.plugin.api.actions.filesplitting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.storage.DataObject;

public class FileContainer
{
	private static String PATH_SEPARATOR = "/";
	
	private long containermaxsize;
	private long containersize;
	private String containerpath;
	private List<DataObject> dataobjects;
	
	public FileContainer (String containerpath, long containermaxsize)
	{
		this.containerpath = containerpath;
		this.containermaxsize = containermaxsize;
		this.containersize = 0;
		
		dataobjects = new ArrayList<DataObject> ();
	}
	
	/**
	 * Tries to add an element to the container.
	 * If the container is full the function returns false.
	 * Else the element would be added to the container and true is returned. 
	 * 
	 * @param data
	 * @return
	 */
	public boolean addData (DataObject data)
	{
		if ((containersize + data.getLength ()) > containermaxsize)
		{
			return false;
		}
		
		dataobjects.add (data);
		containersize += data.getLength ();
		
		return true;
	}
	
	public void setContainerpath (String containerpath)
	{
		this.containerpath = containerpath;
	}
	
	public String getContainerpath ()
	{
		return containerpath;
	}
	
	public int getContainerElementCount ()
	{
		return dataobjects.size ();
	}
	
	public InputStream getContainerElementData (int index) throws IOException
	{
		InputStream is = new ByteArrayInputStream (dataobjects.get (index).getBytes ());
		return is;
	}
	
	public String getContainerElementNewPath (int index)
	{
		return PATH_SEPARATOR + containerpath + dataobjects.get (index).getPath ();
	}
	
	public String getContainerElementOldPath (int index)
	{
		return dataobjects.get (index).getPath ();
	}
	
	public long getContainerElementLength (int index)
	{
		return dataobjects.get (index).getLength ();
	}
	
	public MetainfoContainer getContainerElementMetaInfo (int index)
	{
		return dataobjects.get (index).getMetainfo ();
	}
	
	public long getContainersize ()
	{
		return containersize;
	}
}
