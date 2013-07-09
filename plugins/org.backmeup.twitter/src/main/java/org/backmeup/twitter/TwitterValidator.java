package org.backmeup.twitter;

import java.util.Map;
import java.util.Properties;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;

import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


/**
 * This Validator makes sure, that authorization works fine, 
 * that there are remaining API calls and that API works.
 * @mmurauer 
 *
 */
public class TwitterValidator implements Validationable {

	
	@Override
	public ValidationNotes validate(Properties accessData) {
		ValidationNotes notes = new ValidationNotes();
		
		//1. Make sure authentication / authorization is working well
		//check if accesstoken != null
		AccessToken at = new AccessToken(accessData.getProperty("token"), accessData.getProperty("secret"));
		
		TwitterHelper th = TwitterHelper.getInstance();
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true).setOAuthConsumerKey(th.getAppKey()).setOAuthConsumerSecret(th.getAppSecret())
          .setOAuthAccessToken(at.getToken()).setOAuthAccessTokenSecret(at.getTokenSecret());
       
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        
        try {
			twitter.getOAuthAccessToken();
		} catch (TwitterException e) {
			notes.addValidationEntry(ValidationExceptionType.AuthException, TwitterDescriptor.TWITTER_ID, e);
		}
        
        try {
        	//2. get ratelimit from API, to make sure it is working fine
        	Map<String, RateLimitStatus> rls = twitter.getRateLimitStatus();
			//3. check if there are remainig hits left 
        	if(rls.get("statuses").getRemaining()<=0){
        		notes.addValidationEntry(ValidationExceptionType.APIException, TwitterDescriptor.TWITTER_ID, new Exception("No remaining API calls left!"));
        	}
        	if(rls.get("user").getRemaining()<=0){
        		notes.addValidationEntry(ValidationExceptionType.APIException, TwitterDescriptor.TWITTER_ID, new Exception("No remaining API calls left!"));
        	}
		} catch (TwitterException e1) {
			notes.addValidationEntry(ValidationExceptionType.APIException, TwitterDescriptor.TWITTER_ID, e1);
		}
        
        return notes;
	}

}
