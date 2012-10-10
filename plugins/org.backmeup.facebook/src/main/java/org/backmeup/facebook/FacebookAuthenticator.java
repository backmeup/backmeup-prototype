package org.backmeup.facebook;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.spi.OAuthBased;
import org.json.JSONObject;


/**
 * The FacebookAuthenticator creates a redirect URL based on
 * the app informations and stores Accesstoken in the inputProperties
 * 
 * @author mmurauer
 * 
 */
public class FacebookAuthenticator implements OAuthBased {

	String callback = "";
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.OAuth;
	}

	@Override
	public String createRedirectURL(Properties inputProperties, String callback) {
		this.callback = callback;
		
		FacebookHelper fh = FacebookHelper.getInstance();
		
		return "https://www.facebook.com/dialog/oauth?client_id=" + fh.getAppKey() +
				"&return_session=true&redirect_uri="+callback+"&scope="
				+ "user_birthday,user_photos,read_stream,user_about_me,user_activities," +
				"user_education_history,user_events,user_groups,user_hometown,user_interests" +
				",user_likes,user_location,user_notes,user_questions,user_relationships," +
				"user_relationship_details,user_religion_politics,user_status," +
				"user_subscriptions,user_videos,user_website,user_work_history,email," +
				"read_friendlists,friends_photos, friends_about_me, friends_activities, friends_birthday, " +
				"friends_education_history, friends_hometown, " +
				"friends_interests, friends_likes, friends_location, friends_relationships, " +
				"friends_religion_politics, friends_website, friends_work_history, " +
				"manage_pages"; 
	}

	@Override
	public void postAuthorize(Properties inputProperties) {
		String code = inputProperties.getProperty("code");
		HttpURLConnection c = null;
		URL url;
		try {
			url = new URL("https://graph.facebook.com/oauth/access_token?" +
					"client_id="+FacebookHelper.getInstance().getAppKey()+
					"&redirect_uri=http://www.backmeup.at/"+
					"&client_secret="+FacebookHelper.getInstance().getAppSecret()+"&code="+code);
		
			c = (HttpURLConnection) url.openConnection();
			c.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					c.getInputStream(), Charset.forName("UTF-8")));
			StringBuilder content = new StringBuilder();

			int temp;

			while ((temp = reader.read()) != -1) {
				content.append((char) temp);
			}
			
			if(content.toString().startsWith("access_token")){
				String[] params = content.toString().split("&");
				for (String param : params){
					String name = param.split("=")[0];
					String value = param.split("=")[1];
					if(name.equals("access_token")){
						inputProperties.setProperty(FacebookHelper.PROPERTY_TOKEN, value);
					}
				}
			}else throw new PluginException(FacebookDescriptor.FACEBOOK_ID, "An error occurred while retrieving authentication information");
			
			
		} catch (Exception e) {
			throw new PluginException(FacebookDescriptor.FACEBOOK_ID, "An error occurred while retrieving authentication information", e);
		}
	}


}
