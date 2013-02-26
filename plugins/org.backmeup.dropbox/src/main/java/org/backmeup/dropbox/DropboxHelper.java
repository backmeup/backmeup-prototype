package org.backmeup.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.backmeup.model.exceptions.InvalidKeyException;
import org.backmeup.model.exceptions.PluginException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;

/**
 * This Helper class constructs and configures the DropboxAPI element.
 * It uses dropbox.properties found within the bundles jar file
 * to retrieve the access token + secret token.
 * 
 * @author fschoeppl
 *
 */
public class DropboxHelper {
	
	public static final String PROPERTY_TOKEN = "token";
	
	public static final String PROPERTY_SECRET = "secret";
	
	private String appKey;
	
	private String appSecret;
	
	private DropboxHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("dropbox.properties");
		if (is == null)
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, "Fatal error: cannot find dropbox.properties within jar-file!");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, "Fatal error: could not load dropbox.properties: " + e.getMessage(), e);
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
	
	public static DropboxAPI<WebAuthSession> getApi(Properties items) {		
		String token = items.getProperty(DropboxHelper.PROPERTY_TOKEN);
		String secret = items.getProperty(DropboxHelper.PROPERTY_SECRET);
		WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();
		session.setAccessTokenPair(new AccessTokenPair(token, secret));
		if (!session.isLinked()) {
			throw new InvalidKeyException("org.backmeup.dropbox", "userToken, userSecret", token + ", " + secret, "dropbox.properties");
		}
		return new DropboxAPI<WebAuthSession>(session);
	}
	
}