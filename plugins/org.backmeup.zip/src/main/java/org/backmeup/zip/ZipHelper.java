package org.backmeup.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.backmeup.model.exceptions.PluginException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ZipHelper {
  private Logger logger = Logger.getLogger(ZipDatasink.class.getName());
  
  private String temporaryPath;
  private boolean isRemote;
  private String host;
  private String user;
  private String target;
  private String remoteDirectory;
  private int port;
  private String sshkey;    

  public ZipHelper() {
    InputStream is = getClass().getClassLoader().getResourceAsStream(
        "zip.properties");
    if (is == null)
      throw new PluginException(ZipDescriptor.ZIP_ID,
          "Fatal error: cannot find zip.properties within jar-file!");

    try {
      Properties properties = new Properties();
      properties.load(is);
      temporaryPath = properties.getProperty("temporaryPath");
      isRemote = Boolean.parseBoolean(properties.getProperty("isRemote"));
      host = properties.getProperty("remote.host");
      user = properties.getProperty("remote.user");
      target = properties.getProperty("remote.target");
      remoteDirectory = properties.getProperty("remote.directory");
      port = Integer.parseInt(properties.getProperty("remote.port"));
      sshkey = properties.getProperty("ssh.key");
    } catch (IOException e) {
      throw new PluginException(ZipDescriptor.ZIP_ID,
          "Fatal error: could not load zip.properties: " + e.getMessage(), e);
    } finally {
      try {
        if (is != null)
          is.close();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  public static ZipHelper getInstance() {
    return new ZipHelper();
  }

  public String getTemporaryPath() {
    return temporaryPath;
  }

  public boolean isRemote() {
    return isRemote;
  }

  private Session getSshSession() {
    try {
      JSch jsch = new JSch();
      jsch.addIdentity(sshkey);
      Session session = jsch.getSession(user, host, port);
      session.setConfig("StrictHostKeyChecking", "no");
      return session;
    } catch (JSchException e) {
      throw new PluginException(ZipDescriptor.ZIP_ID, "Couldn't create sftp channel!", e);
    }
  }

  private ChannelSftp getSftpChannel() {
    try {
      Session session = getSshSession();
      session.connect();
      Channel channel = session.openChannel("sftp");
      channel.connect();
      ChannelSftp sftpChannel = (ChannelSftp) channel;
      return sftpChannel;
    } catch (JSchException e) {
      logger.log(Level.FINE, "Couldn't create sftp channel!", e);      
      throw new PluginException(ZipDescriptor.ZIP_ID, "Couldn't create sftp channel!", e);      
    }
  }

  public void sendToSftpDestination(InputStream zipStream, String fileName, String userId) {
    ChannelSftp sftpChannel = getSftpChannel();
    try {
      try {
        String absolutePath = MessageFormat.format(remoteDirectory, userId);
        File f = new File(absolutePath);
        Stack<String> paths = new Stack<String>();
        File current = f;
        while (current != null) {
          if (!current.getAbsolutePath().equals("/"))
            paths.push(current.getAbsolutePath());
          current = current.getParentFile();
        }
        
        String path = null;
        while (!paths.isEmpty()) {
          path = paths.pop();
          try {
            sftpChannel.mkdir(path);
          } catch (Exception ex) {
            // ignore mkdir exceptions
          }
        }
      } catch (Exception ex) {}
      sftpChannel.put(zipStream, MessageFormat.format(target, userId, fileName));      
      sftpChannel.disconnect();      
    } catch (SftpException e) {
      logger.log(Level.FINE, "Failed to put file via sftp", e);
      throw new PluginException(ZipDescriptor.ZIP_ID, "Failed to put file via sftp!", e);
    }
  }
}
