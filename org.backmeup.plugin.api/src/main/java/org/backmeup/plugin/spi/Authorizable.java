package org.backmeup.plugin.spi;

import java.util.Properties;


public interface Authorizable {

	public static final String PROP_REDIRECT_URL = "PROP_REDIRECT_URL";
	
	public enum AuthorizationType {
		OAuth,
		InputBased
	}
	
	public AuthorizationType getAuthType(); 
	
	// updates the authorization data and returns the identification of the account
	public String postAuthorize(Properties inputProperties);
		
}
