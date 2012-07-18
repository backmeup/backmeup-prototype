package org.backmeup.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * TwitterHelper offers application key and secret
 * @author user mmurauer
 *
 */
public class TwitterHelper {

	public static final String PROPERTY_TOKEN = "token";
	
	public static final String PROPERTY_SECRET = "secret";
	
	private String appKey;
	private String appSecret;
	
	private TwitterHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("twitter.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: twitter.properties not found");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load twitter.properties: " + e.getMessage());
		}
		
		appKey = properties.getProperty("app.key");
		appSecret = properties.getProperty("app.secret");
	}

	public static TwitterHelper getInstance() {
		return new TwitterHelper();
	}

	public String getAppKey() {
		return appKey;
	}
	
	public String getAppSecret() {
		return appSecret;
	}
}
