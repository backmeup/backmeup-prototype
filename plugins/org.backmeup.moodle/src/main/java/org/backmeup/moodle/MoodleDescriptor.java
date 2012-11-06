package org.backmeup.moodle;

import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

/**
 * This class provides all necessary information about the plugin.
 * 
 * @author florianjungwirth
 *
 */
public class MoodleDescriptor implements SourceSinkDescribable {

	public static final String MOODLE_ID = "org.backmeup.moodle";
	
	@Override
	public String getDescription() {
		return "Moodle Plugin for Backmeup";
	}

	@Override
	public String getId() {
		return MOODLE_ID;
	}

	@Override
	public String getTitle() {
		return "BackMeUp Moodle Plug-In";
	}

	@Override
	public String getImageURL() {
		return "http://moodle.org/logo/logo-4045x1000.jpg";
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
