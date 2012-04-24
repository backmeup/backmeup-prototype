package org.backmeup.logic.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

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
}
