package org.backmeup.facebook;

import java.util.Properties;
import org.backmeup.plugin.spi.OAuthBased;


/**
 * The FacebookAuthenticator creates a redirect URL based on
 * the app informations and stores Accesstoken in the inputProperties
 * 
 * @author mmurauer
 * 
 */
public class FacebookAuthenticator implements OAuthBased {

	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.OAuth;
	}

	@Override
	public String createRedirectURL(Properties inputProperties, String callback) {
		FacebookHelper fh = FacebookHelper.getInstance();
		
		return "https://www.facebook.com/dialog/oauth?client_id="
				+ fh.getAppKey() + "&return_session=true&redirect_uri="
				+ callback + "&response_type=token&scope="
				+ "user_birthday,user_photos,read_stream,user_about_me,user_activities," +
				"user_education_history,user_events,user_groups,user_hometown,user_interests" +
				",user_likes,user_location,user_notes,user_questions,user_relationships," +
				"user_relationship_details,user_religion_politics,user_status," +
				"user_subscriptions,user_videos,user_website,user_work_history,email," +
				"read_friendlists,friends_photos, friends_about_me, friends_activities, friends_birthday, friends_education_history, friends_hometown, " +
				"friends_interests, friends_likes, friends_location, friends_relationships, " +
				"friends_religion_politics, friends_website, friends_work_history, " +
				"manage_pages"; // TODO add more permissions
	}

	@Override
	public void postAuthorize(Properties inputProperties) {
		String accessToken = inputProperties.getProperty("access_token");
		inputProperties.setProperty(FacebookHelper.PROPERTY_TOKEN, accessToken);
		
	}


}
