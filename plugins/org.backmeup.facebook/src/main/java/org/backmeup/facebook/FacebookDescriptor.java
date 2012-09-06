package org.backmeup.facebook;

import java.util.Properties;
import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

/**
 * This class provides all necessary information about the plugin.
 * 
 * @author mmurauer
 *
 */
public class FacebookDescriptor implements SourceSinkDescribable {
	public static final String FACEBOOK_ID = "org.backmeup.facebook";

	@Override
	public String getId() {
		return FACEBOOK_ID;
	}

	@Override
	public String getTitle() {
		return "BackMeUp Facebook Plug-In";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of downloading from facebook";
	}

	@Override
	public String getImageURL() {
		// from graph.facebook.com/facebook picture
		return "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc4/174597_20531316728_2866555_s.jpg";
	}

	@Override
	public Type getType() {
		return Type.Source;
	}

	@Override
	public Properties getMetadata(Properties accessData) {
		Properties metadata = new Properties();
		metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
		return metadata;
	}

}
