package org.backmeup.facebook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * offers application key and secret 
 * 
 * @author mmurauer
 *
 */
public class FacebookHelper {
	public static final String PROPERTY_TOKEN = "token";
	
	public static final String PROPERTY_SECRET = "secret";
	
	private String appKey;
	private String appSecret;
	
	private FacebookHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("facebook.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: facebook.properties not found");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load facebook.properties: " + e.getMessage());
		}
		
		appKey = properties.getProperty("app.key");
		appSecret = properties.getProperty("app.secret");
	}

	public static FacebookHelper getInstance() {
		return new FacebookHelper();
	}

	public String getAppKey() {
		return appKey;
	}
	
	public String getAppSecret() {
		return appSecret;
	}

}
