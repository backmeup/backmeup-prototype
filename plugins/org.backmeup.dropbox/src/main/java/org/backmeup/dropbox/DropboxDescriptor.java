package org.backmeup.dropbox;

import org.backmeup.model.spi.SourceSinkDescribable;
/**
 * The DropboxDescriptor provides all necessary
 * information about this plugin.
 * Note: DROPBOX_ID matches the filters stated in the
 *       configuration files:
 *         META-INF/spring/org.backmeup.dropbox-context.xml
 *         META-INF/spring/org.backmeup.dropbox-osgi-context.xml
 *         
 * @author fschoeppl
 */
public class DropboxDescriptor implements SourceSinkDescribable {
	public static final String DROPBOX_ID = "org.backmeup.dropbox";
	
	@Override
	public String getId() {
		return DROPBOX_ID;
	}

	@Override
	public String getTitle() {
		return "BackMeUp Dropbox Plug-In";
	}

	@Override
	public String getDescription() {
		return "A plug-in that is capable of downloading and uploading from dropbox";
	}

	@Override
	public String getImageURL() {
		return "http://about:blank";
	}

	@Override
	public Type getType() {
		return Type.Both;
	}

}
