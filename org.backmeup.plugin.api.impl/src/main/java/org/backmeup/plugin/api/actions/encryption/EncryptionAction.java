package org.backmeup.plugin.api.actions.encryption;

import java.util.Properties;

import org.backmeup.plugin.api.actions.Action;
import org.backmeup.plugin.api.actions.ActionException;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.StorageReader;

public class EncryptionAction implements Action
{
	private final String PROP_CONT_COUNT = "org.backmeup.filesplitting.containercount";
	private final String PROP_PASSWORD = "org.backmeup.encryption.password";
	
	@Override
	public String doAction (Properties accessData, StorageReader storage, Progressable progressor) throws ActionException
	{
		String password;
		int containers;
		long[] containersize;
		
		if (accessData.containsKey (PROP_CONT_COUNT) == true)
		{
			containers = new Integer (accessData.getProperty (PROP_CONT_COUNT));
		}
		else
		{
			throw new ActionException ("Property \"" + PROP_CONT_COUNT + "\" is not set");
		}
		
		containersize = new long[containers];
		for (int i = 0; i < containers; i++)
		{
			if (accessData.containsKey ("org.backmeup.filesplitting.container." + i + ".size") == true)
			{
				containersize[i] = new Long (accessData.getProperty ("org.backmeup.filesplitting.container." + i + ".size"));
				// add 10% to size for filesystem
				containersize[i] += ((containersize[i] / 100) * 10);
			}
			else
			{
				throw new ActionException ("Property \"org.backmeup.filesplitting.container." + i + ".size\" is not set");
			}
		}
		
		if (accessData.containsKey (PROP_PASSWORD) == true)
		{
			password = accessData.getProperty (PROP_PASSWORD);
		}
		else
		{
			throw new ActionException ("Property \"" + PROP_PASSWORD + "\" is not set");
		}
		
		// TODO create container(s)
		// TODO move files to container(s)
		
		return "";
	}
}
