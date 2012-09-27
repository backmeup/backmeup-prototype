package org.backmeup.plugin.api.actions.encryption;

import java.util.Iterator;
import java.util.Properties;

import org.backmeup.model.BackupJob;
import org.backmeup.plugin.api.MetainfoContainer;
import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;

public class EncryptionAction implements Action
{
	private final String PROP_CONT_COUNT = "org.backmeup.filesplitting.containercount";
	private final String PROP_PASSWORD = "org.backmeup.encryption.password";
	
	@Override
	public void doAction (Properties parameters, Storage storage, BackupJob job, Progressable progressor) throws ActionException
	{
		String password;
		int containers;
		long[] containersize;
		String[] containername;
		
		if (parameters.containsKey (PROP_CONT_COUNT) == true)
		{
			containers = new Integer (parameters.getProperty (PROP_CONT_COUNT));
		}
		else
		{
			throw new ActionException ("Property \"" + PROP_CONT_COUNT + "\" is not set");
		}
		
		containersize = new long[containers];
		containername = new String[containers];
		for (int i = 0; i < containers; i++)
		{
			if (parameters.containsKey ("org.backmeup.filesplitting.container." + i + ".size") == true)
			{
				containersize[i] = new Long (parameters.getProperty ("org.backmeup.filesplitting.container." + i + ".size"));
				// add 10% to size for filesystem
				containersize[i] += ((containersize[i] / 100) * 10);
			}
			else
			{
				throw new ActionException ("Property \"org.backmeup.filesplitting.container." + i + ".size\" is not set");
			}
			
			if (parameters.containsKey ("org.backmeup.filesplitting.container." + i + ".name") == true)
			{
				containername[i] = parameters.getProperty ("org.backmeup.filesplitting.container." + i + ".name");
			}
			else
			{
				throw new ActionException ("Property \"org.backmeup.filesplitting.container." + i + ".name\" is not set");
			}
		}
		
		if (parameters.containsKey (PROP_PASSWORD) == true)
		{
			password = parameters.getProperty (PROP_PASSWORD);
		}
		else
		{
			throw new ActionException ("Property \"" + PROP_PASSWORD + "\" is not set");
		}
		
		EncryptionContainers enccontainers = new EncryptionContainers (containers, containersize, containername, password);
		try
		{
			Iterator<DataObject> dataObjects = storage.getDataObjects ();
			while (dataObjects.hasNext () == true)
			{
				DataObject daob = dataObjects.next ();
				enccontainers.addFile (daob);
			}
		}
		catch (Exception e)
		{
			throw new ActionException (e);
		}
		
		for (EncryptionContainer container : enccontainers.getContainers ())
		{
			try
			{
				container.writeContainer ();
				storage.addFile (container.getContainer (), container.getContainername (), new MetainfoContainer ());
				container.deleteContainer ();
				
				for (DataObject daob : container.getData ())
				{
					String[] parts = daob.getPath ().split ("/");
					String path = "";
					for (int i = 2; i < parts.length; i++)
					{
						path += "/" + parts[i];
					}
					
					storage.removeFile (daob.getPath ());
				}
			}
			catch (Exception e)
			{
				throw new ActionException (e);
			}
		}
		
		enccontainers.cleanupFolders ();
	}
}
