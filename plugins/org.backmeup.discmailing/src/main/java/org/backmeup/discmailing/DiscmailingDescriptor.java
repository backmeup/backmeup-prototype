package org.backmeup.discmailing;

import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;

public class DiscmailingDescriptor implements SourceSinkDescribable {
	public static final String DISC_ID = "org.backmeup.discmailing";
	
	@Override
	public String getId() {
		return DISC_ID;
	}

	@Override
	public String getTitle() {
		return "Discmailing";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of uploading data to DVD burning station";
	}

	@Override
	public Properties getMetadata(Properties accessData) {
		Properties metadata = new Properties();
		metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
		
		return metadata;
	}

	@Override
	public Type getType() {
		return Type.Sink;
	}

	@Override
	public String getImageURL() {
		return "https://backmeup.at/dummy.png";
	}

}

