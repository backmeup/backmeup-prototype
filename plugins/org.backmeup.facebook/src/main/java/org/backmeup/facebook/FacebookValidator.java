package org.backmeup.facebook;

import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookGraphException;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;

/**
 * The FacebookValidator makes sure that authorization was successfully
 * and checks if API-calls are working fine.
 * 
 * @author mmurauer
 * 
 */

public class FacebookValidator implements Validationable {

	@Override
	public ValidationNotes validate(Properties arg0) {
		ValidationNotes notes = new ValidationNotes();
		try {
			// Make sure authentication / authorization and API is working well
			String accessToken = arg0.getProperty("token");
			if (accessToken == null)
				notes.addValidationEntry(ValidationExceptionType.AuthException, FacebookDescriptor.FACEBOOK_ID);

			FacebookClient client = new DefaultFacebookClient(accessToken);
			
			// Just to be sure about API is working well, catch user information
			User user = client.fetchObject("me", User.class);

		} catch (FacebookNetworkException e) {
			notes.addValidationEntry(ValidationExceptionType.APIException, FacebookDescriptor.FACEBOOK_ID, e);
		} catch (FacebookOAuthException e) {
			notes.addValidationEntry(ValidationExceptionType.AuthException, FacebookDescriptor.FACEBOOK_ID, e);
		} catch (FacebookGraphException e) {
			notes.addValidationEntry(ValidationExceptionType.APIException, FacebookDescriptor.FACEBOOK_ID, e);
		}
		
		return notes;
	}

}
