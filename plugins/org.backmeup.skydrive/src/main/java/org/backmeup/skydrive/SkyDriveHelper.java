package org.backmeup.skydrive;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 * The helper class retrieves 
 * the application token + secret from the 
 * configuration file skydrive.properties which must be part 
 * of the bundle-jar-file.
 *  
 * @author fschoeppl
 *
 */
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
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load skydrive.properties: " + e.getMessage());
		} finally {
		  try {
		    is.close();
		  } catch(Exception ex) {
		    ex.printStackTrace();
		  }
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