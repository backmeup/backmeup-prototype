package org.backmeup.dropbox;

import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.spi.OAuthBased;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

/**
 * The DropboxAuthenticator creates a redirect URL based on the
 * Dropbox API and stores all information needed for further
 * authentication/authorization within the inputProperties parameter.
 * 
 * @author fschoeppl
 *
 */
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
			inputProperties.setProperty(DropboxHelper.PROPERTY_TOKEN, rtp.key);
			inputProperties.setProperty(DropboxHelper.PROPERTY_SECRET, rtp.secret);
			return authInfo.url + "&oauth_callback=" + callback;
		} catch (DropboxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred while retrieving authentication information", e);
		}
	}

	@Override
	public String postAuthorize(Properties inputProperties) {
		// Retrieve auth info from DB
		try {
			WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();

			String token = inputProperties.getProperty(DropboxHelper.PROPERTY_TOKEN);
			String secret = inputProperties.getProperty(DropboxHelper.PROPERTY_SECRET);

			session.setAccessTokenPair(new AccessTokenPair(token, secret));
			session.retrieveWebAccessToken(new RequestTokenPair(token, secret));
			// Update access token in DB
			AccessTokenPair atp = session.getAccessTokenPair();
			inputProperties.setProperty(DropboxHelper.PROPERTY_TOKEN, atp.key);
			inputProperties.setProperty(DropboxHelper.PROPERTY_SECRET,
					atp.secret);
			
			return DropboxHelper.getApi(inputProperties).accountInfo().displayName;
			
		} catch (DropboxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred during post authorization", e);
		}
	}

}
