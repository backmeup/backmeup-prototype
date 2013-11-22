package org.backmeup.plugin.api.actions.filesplitting;

import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.storage.DataObject;

public class FileContainers
{
	private static int CONTAINER_LIMIT = 999;
	private static String PART_NUM_FORMAT = "%03d";
	private static final String CONTAINER_CRYPT_EXTENSION = ".tc";
	
	private List<FileContainer> filecontainers;
	private long containermaxsize;
	private FileContainer currcontainer;
	boolean encrypt;
	
	public FileContainers (long containermaxsize, boolean encrypt)
	{
		this.containermaxsize = containermaxsize;
		this.encrypt = encrypt;
		
		filecontainers = new LinkedList<FileContainer> ();
		currcontainer = null;
	}
	
	private String getNewContainerpath () throws ActionException
	{
		String containerpath = "";
		
		if (encrypt == false)
		{
			containerpath = "part" + String.format (PART_NUM_FORMAT, filecontainers.size () + 1);
		}
		else
		{
			containerpath = "part" + String.format (PART_NUM_FORMAT, filecontainers.size () + 1) + CONTAINER_CRYPT_EXTENSION;	
		}
		
		if (filecontainers.size () > CONTAINER_LIMIT)
		{
			// TODO throw new ToMuchSplitt...
			throw new ActionException ("Can't split data in more than " + CONTAINER_LIMIT + " containers");
		}
		
		return containerpath;
	}
	
	public void addData (DataObject data) throws ActionException
	{	
		if (data.getLength () > containermaxsize)
		{
			// TODO throw new FileToBig...
			throw new ActionException ("File (path: " + data.getPath () + ", size: " + data.getLength () + ") is bigger than maximum container size (" + containermaxsize + ").");
		}
		
		if (currcontainer == null)
		{
			currcontainer = new FileContainer (getNewContainerpath(), containermaxsize);
		}
		
		if (currcontainer.addData (data) == false)
		{
			filecontainers.add (currcontainer);
			currcontainer = new FileContainer (getNewContainerpath (), containermaxsize);
			currcontainer.addData (data);
		}
	}
	
	public void finish ()
	{
		filecontainers.add (currcontainer);
	}
	
	public List<FileContainer> getContainers ()
	{
		// Remove the _part001 extension if only one part exists
		if (filecontainers.size () == 1)
		{
			String new_containerpath = filecontainers.get (0).getContainerpath ();
			new_containerpath = new_containerpath.replaceAll ("_part001", "");
			
			filecontainers.get (0).setContainerpath (new_containerpath);
		}
		
		return filecontainers;
	}
}
