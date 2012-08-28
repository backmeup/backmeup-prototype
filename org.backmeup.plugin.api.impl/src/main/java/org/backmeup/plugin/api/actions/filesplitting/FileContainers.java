package org.backmeup.plugin.api.actions.filesplitting;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.storage.DataObject;

public class FileContainers
{
	private static int CONTAINER_LIMIT = 999;
	
	private List<FileContainer> filecontainers;
	private String containerpath;
	private long containermaxsize;
	private FileContainer currcontainer;
	
	public FileContainers (String containerpath, long containermaxsize)
	{
		this.containerpath = containerpath;
		this.containermaxsize = containermaxsize;
		
		filecontainers = new ArrayList<FileContainer> ();
		currcontainer = new FileContainer (containerpath + "_000", containermaxsize);
	}
	
	private String getNewContainerpath () throws ActionException
	{
		if (filecontainers.size () < 10)
		{
			return containerpath + "_00" + filecontainers.size ();
		}
		
		if (filecontainers.size () > 10)
		{
			return containerpath + "_0" + filecontainers.size ();
		}
		
		if (filecontainers.size () > 100)
		{
			return containerpath + "_" + filecontainers.size ();
		}
		
		if (filecontainers.size () > CONTAINER_LIMIT)
		{
			// TODO throw new ToMuchSplitt...
			throw new ActionException ("Cant split data in more than " + CONTAINER_LIMIT + " containers");
		}
		
		return "";
	}
	
	public void addData (DataObject data) throws ActionException
	{	
		if (data.getLength () > containermaxsize)
		{
			// TODO throw new FileToBig...
			throw new ActionException ("File (path: " + data.getPath () + ", size: " + data.getLength () + ") is bigger than maximum container size (" + containermaxsize + ").");
		}
		
		if (currcontainer.addData (data) == false)
		{
			filecontainers.add (currcontainer);
			currcontainer = new FileContainer (getNewContainerpath (), containermaxsize);
			currcontainer.addData (data);
		}
	}
	
	public List<FileContainer> getContainers ()
	{
		return filecontainers;
	}
}
