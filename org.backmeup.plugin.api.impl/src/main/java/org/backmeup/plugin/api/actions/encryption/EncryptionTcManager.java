package org.backmeup.plugin.api.actions.encryption;

public class EncryptionTcManager
{
	public void createContainer (EncryptionContainer container)
	{
		ProcessBuilder pb = new ProcessBuilder ("sudo", "/usr/local/sbin/create_container.sh", container.getContainer_path (), container.getMountpoint (), container.getSize () + "", container.getPassword ());
		try
		{
			Process process = pb.start ();
			if (process.waitFor () != 0)
			{
				// TODO Something happens
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}
	
	public void unmountContainer (EncryptionContainer container)
	{
		ProcessBuilder pb = new ProcessBuilder ("sudo", "/usr/local/sbin/umount_container.sh", container.getContainer_path ());
		try
		{
			Process process = pb.start ();
			if (process.waitFor () != 0)
			{
				// TODO Something happens
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace ();
		}
	}
}
