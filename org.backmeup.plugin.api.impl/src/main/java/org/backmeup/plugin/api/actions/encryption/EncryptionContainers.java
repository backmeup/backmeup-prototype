package org.backmeup.plugin.api.actions.encryption;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.backmeup.plugin.api.storage.DataObject;

public class EncryptionContainers
{
	HashMap<String, EncryptionContainer> containers;
	
	public EncryptionContainers (int containercount, long[] containersize, String containername)
	{
		containers = new HashMap<String, EncryptionContainer> ();
		
		for (int i = 0; i < containercount; i++)
		{
			EncryptionContainer container = new EncryptionContainer ();
			containers.put (containername, container);
		}
	}
	
	public void addFile (DataObject daob)
	{
		String container_name = daob.getPath ().split (System.getProperty ("file.separator"))[0];
		
		if (containers.containsKey (container_name) == false)
		{
			EncryptionContainer container = new EncryptionContainer ();
			containers.put (container_name, container);
		}
		
		containers.get (container_name).addData (daob);
	}
	
	public List<EncryptionContainer> getContainers ()
	{		
		return new LinkedList<EncryptionContainer> (containers.values ());
	}
}
