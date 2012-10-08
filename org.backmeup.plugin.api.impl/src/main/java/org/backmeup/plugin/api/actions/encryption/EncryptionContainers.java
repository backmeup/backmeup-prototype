package org.backmeup.plugin.api.actions.encryption;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.backmeup.plugin.api.storage.DataObject;

public class EncryptionContainers
{
	private final String FS_PATH_SEPARATOR = System.getProperty ("file.separator");
	private final String SYS_TEMP_DIR = System.getProperty ("java.io.tmpdir");
	private String containers_temp_dir;
	
	HashMap<String, EncryptionContainer> containers;
	
	public EncryptionContainers (int containercount, long[] containersize, String[] containername, String password)
	{
		containers = new HashMap<String, EncryptionContainer> (containercount);
		
		containers_temp_dir = RandomStringUtils.randomAlphanumeric (16);
		
		for (int i = 0; i < containercount; i++)
		{
			String containerpath = SYS_TEMP_DIR + FS_PATH_SEPARATOR + containers_temp_dir + FS_PATH_SEPARATOR + containername[i];
			String mountpoint = SYS_TEMP_DIR + FS_PATH_SEPARATOR + containers_temp_dir + FS_PATH_SEPARATOR + "mnt" + FS_PATH_SEPARATOR + containername[i];
			EncryptionContainer container = new EncryptionContainer (containername[i], containerpath, mountpoint, password, containersize[i]);
			containers.put (containername[i], container);
		}
	}
	
	public void addFile (DataObject daob)
	{
		String containername = daob.getPath ().split (FS_PATH_SEPARATOR)[1];
		
		containers.get (containername).addData (daob);
	}
	
	public List<EncryptionContainer> getContainers ()
	{		
		return new LinkedList<EncryptionContainer> (containers.values ());
	}
	
	public void cleanupFolders ()
	{
		deleteFiles (SYS_TEMP_DIR + FS_PATH_SEPARATOR + containers_temp_dir);
	}
	
	private void deleteFiles (String path)
	{
		File file = new File (path);
		if (file.exists () == true)
		{
			if (file.isDirectory ())
			{
				for (File subfile : file.listFiles ())
				{
					deleteFiles (subfile.getPath ());
				}
			}
			else
			{
				file.delete ();
			}
		}
	}
}
