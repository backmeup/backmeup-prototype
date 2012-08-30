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
import org.backmeup.plugin.api.storage.StorageException;
import org.backmeup.plugin.api.storage.StorageReader;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class DiscmailingDatasink implements Datasink {

	@Override
	public String upload(Properties items, StorageReader storage,
			Progressable progressor) throws StorageException  {
		
		DiscmailingHelper helper = DiscmailingHelper.getInstance();
		
		String target = helper.getTarget();
         
        Session session = helper.getSshSession();       
        ChannelSftp sftpChannel =  helper.getSftpChannel(session);
        
        if (sftpChannel == null) {
        	throw new PluginException(DiscmailingDescriptor.DISC_ID, "Error during connecting to Server " + session.getHost());
        }
        
        System.out.println("Connected");

        //generate XML Ticket
        try {
        	InputStream in = helper.generateTicket(items);
      		String path = helper.getTicketpath() + "/ticket.xml";
      		sftpChannel.put(in, path);
      	} catch (Exception e) {
      		throw new PluginException(DiscmailingDescriptor.DISC_ID, "Error during upload of file %s", e);
      	}
        
		Iterator<DataObject> it = storage.getDataObjects();
		int i = 1;
		while(it.hasNext()) {
			DataObject dataObj = it.next();
			try {
				byte[] data = dataObj.getBytes();
				InputStream bis = new ByteArrayInputStream(data);
				String path = target + dataObj.getPath();
				String log = String.format("Uploading file %s (Number: %d)...", path, i++);
				System.out.println(log);
				progressor.progress(log);
				try {
					File f = new File(dataObj.getPath());
					if (!directoryExists(target + f.getParent(), sftpChannel)) {
						mkdirRec(path, sftpChannel);
					}					
					sftpChannel.put(bis, path);
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
        System.out.println("upload ok");
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
					System.out.println("mkdir " + curPath);
					sftpChannel.mkdir(curPath);
				}
				catch (Exception e) {
					System.out.println("Error: Folder exists.");					
				}
			}
		}
	}
	
	private boolean directoryExists(String path, ChannelSftp sftpChannel) {
		try {
			sftpChannel.ls(path);
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
}

