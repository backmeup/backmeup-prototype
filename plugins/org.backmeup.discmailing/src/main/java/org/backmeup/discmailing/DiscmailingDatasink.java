package org.backmeup.discmailing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Progressable;
import org.backmeup.plugin.api.storage.DataObject;
import org.backmeup.plugin.api.storage.Storage;
import org.backmeup.plugin.api.storage.StorageException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class DiscmailingDatasink implements Datasink {

	@Override
	public String upload(Properties items, Storage storage,
			Progressable progressor) throws StorageException  {
	
		DiscmailingHelper helper = DiscmailingHelper.getInstance();
		
		String jobid = items.getProperty ("org.backmeup.tmpdir");
		
		if (jobid == null) {
			throw new PluginException(DiscmailingDescriptor.DISC_ID, "Error: Wrong source folder");
		}

		String target = helper.getTarget() + "/" + jobid;
		
        Session session = helper.getSshSession();       
        ChannelSftp sftpChannel =  helper.getSftpChannel(session);
        
        if (sftpChannel == null) {
        	throw new PluginException(DiscmailingDescriptor.DISC_ID, "Error during connecting to Server " + session.getHost());
        }
        
        //generate XML Ticket
    InputStream in = null; 
		try {
        	String ticketPath = helper.getTicketpath() + "/ticket-" + jobid + ".xml";
      		String dataPath = target + "/";
      		in = helper.generateTicket(items, dataPath);
      		sftpChannel.put(in, ticketPath);
      	} catch (Exception e) {
      		throw new PluginException(DiscmailingDescriptor.DISC_ID, "Error during upload of file %s", e);
      	} finally {
      	  try {
      	    if (in != null)
      	      in.close();
      	  } catch (Exception ex) {
      	    ex.printStackTrace();
      	  }
      	}
		
		int i = 1;
		Iterator<DataObject> it = storage.getDataObjects();
		while(it.hasNext()) {
			DataObject dataObj = it.next();
			try {
				byte[] data = dataObj.getBytes();
				InputStream bis = new ByteArrayInputStream(data);
				String dataPath = target + dataObj.getPath();
				String log = String.format("Uploading file %s (Number: %d)...", dataPath, i++);
				System.out.println(log);
				progressor.progress(log);
				try {
					File f = new File(dataObj.getPath());
					if (!directoryExists(target + f.getParent(), sftpChannel)) {
						mkdirRec(dataPath, sftpChannel);
					}
					sftpChannel.put(bis, escapeChars(dataPath));
				}
				catch (SftpException e) {
                        e.printStackTrace();
                }
				bis.close();
			} catch (IOException e) {
				throw new PluginException(DiscmailingDescriptor.DISC_ID, "Error during upload of file %s", e);
			}
		}
		sftpChannel.exit();
        session.disconnect();
		return null;
	}
	
	private void mkdirRec(String curPath, ChannelSftp sftpChannel) {
		String remoteFileSeparator = "/";
		if (!directoryExists(curPath, sftpChannel)) {
			int nextSeparatorIndex = curPath.lastIndexOf(remoteFileSeparator);
			if (nextSeparatorIndex <= 0) {
				System.out.println("Folder creation Error");
			}
			else {
				curPath = curPath.substring(0, nextSeparatorIndex);
				this.mkdirRec(curPath, sftpChannel);
				try {
					if (!directoryExists(curPath, sftpChannel)) {
						System.out.println("mkdir " + curPath);
						sftpChannel.mkdir(curPath);
					}
				}
				catch (Exception e) {
					System.out.println("Error: Folder exists.");					
				}
			}
		}
	}
	
	private boolean directoryExists(String path, ChannelSftp sftpChannel) {
		try {
			sftpChannel.ls(escapeChars(path));
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	private String escapeChars(String path) {
		String [] find = {"?", "*"};
		String [] replace = {"\\?", "\\*"};
		for (int i = 0; i < find.length; i++){
			path = path.replace(find[i], replace[i]);
		}
		return path;
	}
}

