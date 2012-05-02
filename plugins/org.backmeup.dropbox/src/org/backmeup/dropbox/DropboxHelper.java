package org.backmeup.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxHelper {
	
	private String appKey;
	
	private String appSecret;
	
	private DropboxHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("dropbox.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: dropbox.properties not found");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load dropbox.properties: " + e.getMessage());
		}
		
		appKey = properties.getProperty("app.key");
		appSecret = properties.getProperty("app.secret");
	}
	
	public static DropboxHelper getInstance() {
		return new DropboxHelper();
	}

	public WebAuthSession getWebAuthSession() {
		AppKeyPair appKeys = new AppKeyPair(appKey, appSecret);
		return new WebAuthSession(appKeys, AccessType.DROPBOX);
	}
	
}