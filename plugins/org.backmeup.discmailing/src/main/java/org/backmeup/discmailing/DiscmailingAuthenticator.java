package org.backmeup.discmailing;

import java.util.Properties;

import org.backmeup.plugin.spi.OAuthBased;

public class DiscmailingAuthenticator implements OAuthBased {

	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.OAuth;
	}

	@Override
	public void postAuthorize(Properties inputProperties) {
		// do nothing
	}

	@Override
	public String createRedirectURL(Properties inputProperties,
			String callbackUrl) {
		return "NOT_NEEDED";
	}


}
