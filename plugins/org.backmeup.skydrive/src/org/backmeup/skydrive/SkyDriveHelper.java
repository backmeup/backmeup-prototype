package org.backmeup.skydrive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SkyDriveHelper {
	
	private String appKey;
	
	private String appSecret;
	
	private SkyDriveHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("skydrive.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: skydrive.properties not found");

		try {
			properties.load(is);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load skydrive.properties: " + e.getMessage());
		}
		
		appKey = properties.getProperty("app.key");
		appSecret = properties.getProperty("app.secret");
	}
	
	public static SkyDriveHelper getInstance() {
		return new SkyDriveHelper();
	}

	public String getAppKey() {
		return appKey;
	}
	
	public String getAppSecret() {
		return appSecret;
	}
	
}