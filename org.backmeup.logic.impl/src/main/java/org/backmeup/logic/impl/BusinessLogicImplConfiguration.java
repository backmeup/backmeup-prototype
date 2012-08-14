package org.backmeup.logic.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
/**
 * The BusinessLogicImplConfiguration class provides
 * access to the bl.properties file.
 * The bl.properties file must at least contain 
 * the callbackUrl property.
 * 
 * @author fschoeppl
 *
 */
public class BusinessLogicImplConfiguration {
	private Properties loadProperties() {
		Properties props = new Properties();
		InputStream is = null;
		try {
			is = getClass().getClassLoader().getResourceAsStream("bl.properties");
			props.load(is);						
		} catch (Exception e) {
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return props;
	}
	
	@Produces
	@Named("callbackUrl")
	public String getCallbackUrl() {
		return this.loadProperties().getProperty("callbackUrl");
	}
	
	@Produces
	@Named("emailVerificationUrl")
	public String getEmailVerificationUrl() {
	  return this.loadProperties().getProperty("emailVerificationUrl");
	}
	
	@Produces
	@Named("minimalPasswordLength")
	public int getMinimalPasswordLength() {
	  String minimalPasswordLength = this.loadProperties().getProperty("minimalPasswordLength");
	  return Integer.parseInt(minimalPasswordLength);
	}	
	
	@Produces
	@Named("emailRegex")
	public String getEmailRegex() {
	  return this.loadProperties().getProperty("emailRegex");
	}
}
