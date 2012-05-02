package org.backmeup.dropbox;

import java.util.Properties;

import org.backmeup.plugin.spi.OAuthBased;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

public class DropboxAuthenticator implements OAuthBased {

	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.OAuth;
	}

	@Override
	public String createRedirectURL(Properties inputProperties, String callback) {
		WebAuthInfo authInfo;
		try {
			authInfo = DropboxHelper.getInstance().getWebAuthSession()
					.getAuthInfo();		
			RequestTokenPair rtp = authInfo.requestTokenPair;
			inputProperties.setProperty(DropboxDatasource.PROPERTY_TOKEN, rtp.key);
			inputProperties.setProperty(DropboxDatasource.PROPERTY_SECRET, rtp.secret);
			return authInfo.url + "&oauth_callback=" + callback;
		} catch (DropboxException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void postAuthorize(Properties inputProperties) {
		// Retrieve auth info from DB
		try {
			WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();

			String token = inputProperties.getProperty(DropboxDatasource.PROPERTY_TOKEN);
			String secret = inputProperties.getProperty(DropboxDatasource.PROPERTY_SECRET);

			session.setAccessTokenPair(new AccessTokenPair(token, secret));
			session.retrieveWebAccessToken(new RequestTokenPair(token, secret));
			// Update access token in DB
			AccessTokenPair atp = session.getAccessTokenPair();
			inputProperties.setProperty(DropboxDatasource.PROPERTY_TOKEN, atp.key);
			inputProperties.setProperty(DropboxDatasource.PROPERTY_SECRET,
					atp.secret);
		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
