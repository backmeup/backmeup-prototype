package org.backmeup.logic.impl;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.backmeup.configuration.Configuration;
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
  
	@Produces
	@Named("callbackUrl")
	public String getCallbackUrl() {
	  return Configuration.getConfig().getProperty("callbackUrl");		
	}
	
	@Produces
	@Named("emailVerificationUrl")
	public String getEmailVerificationUrl() {
	  return Configuration.getConfig().getProperty("emailVerificationUrl");	  
	}
	
	@Produces
	@Named("minimalPasswordLength")
	public int getMinimalPasswordLength() {
	  String minimalPasswordLength = Configuration.getConfig().getProperty("minimalPasswordLength");
	  return Integer.parseInt(minimalPasswordLength);
	}	
	
	@Produces
	@Named("emailRegex")
	public String getEmailRegex() {
	  return Configuration.getConfig().getProperty("emailRegex");
	}
}
