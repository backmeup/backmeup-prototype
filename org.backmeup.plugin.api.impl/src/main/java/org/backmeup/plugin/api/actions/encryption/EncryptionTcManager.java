package org.backmeup.plugin.api.actions.encryption;

import org.backmeup.plugin.api.actions.ActionException;

public class EncryptionTcManager
{
	public void createContainer (EncryptionContainer container) throws ActionException
	{
		ProcessBuilder pb = new ProcessBuilder ("sudo", "/usr/local/sbin/create_container.sh", container.getContainerpath (), container.getMountpoint (), container.getSize () + "", container.getPassword ());
		try
		{
			Process process = pb.start ();
			if (process.waitFor () != 0)
			{
				// TODO Something happens
				throw new ActionException ("Container creation failed");
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			throw new ActionException (e);
		}
	}
	
	public void unmountContainer (EncryptionContainer container) throws ActionException
	{
		ProcessBuilder pb = new ProcessBuilder ("sudo", "/usr/local/sbin/umount_container.sh", container.getContainerpath ());
		try
		{
			Process process = pb.start ();
			if (process.waitFor () != 0)
			{
				// TODO Something happens
				throw new ActionException ("Container creation failed");
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			throw new ActionException (e);
		}
	}
}
