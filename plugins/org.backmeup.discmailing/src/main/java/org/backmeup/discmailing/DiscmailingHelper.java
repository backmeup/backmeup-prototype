package org.backmeup.discmailing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class DiscmailingHelper {
	
	private String appKey;
	
	private String appSecret;

	private String hostname;
	
	private String username;
	
	private String password;
	
	private String destination;
	
	private int port;
	
	private String privateKey;
	
	public DiscmailingHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("discmailing.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: discmailing.properties not found");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load discmailing.properties: " + e.getMessage());
		}
		
		hostname = properties.getProperty("hostname");
		username = properties.getProperty("username");
		password = properties.getProperty("password");
		destination = properties.getProperty("destination");
		port = Integer.parseInt(properties.getProperty("port"));
		privateKey = properties.getProperty("privateKey");
		
		appKey = properties.getProperty("app.key");
		appSecret = properties.getProperty("app.secret");
	}
	
	public Session getSshSession() {
		try {
			JSch jsch = new JSch();
	        jsch.addIdentity(privateKey);
	        Session session = jsch.getSession(username, hostname, port);
	        session.setConfig("StrictHostKeyChecking", "no");
	        return session;
		}
        catch (JSchException e) {
        	System.out.println("no connection");
        	return null;
        }
	}
	
	public ChannelSftp getSftpChannel(Session session) {
		try {
			session.connect();
	        Channel channel = session.openChannel("sftp");
	        channel.connect();
	        ChannelSftp sftpChannel =  (ChannelSftp) channel;
	        return sftpChannel;
		}
        catch (JSchException e) {
        	System.out.println("no connection");
        	return null;
        }
	}

	public String getAppKey() {
		return appKey;
	}
	
	public String getAppSecret() {
		return appSecret;
	}

	public String getHostname() {
		return hostname;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getDestination() {
		return destination;
	}

	public int getPort() {
		return port;
	}

	public String getPrivateKey() {
		return privateKey;
	}
}
