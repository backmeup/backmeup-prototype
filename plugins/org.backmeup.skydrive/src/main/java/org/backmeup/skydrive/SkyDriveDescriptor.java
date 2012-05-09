package org.backmeup.skydrive;

import org.backmeup.model.spi.SourceSinkDescribable;
/**
 * The SkyDriveDescriptor provides all necessary
 * information about this plugin.
 * Note: SKYDRIVE_ID matches the filters stated in the
 *       configuration files:
 *         META-INF/spring/org.backmeup.skydrive-context.xml
 *         META-INF/spring/org.backmeup.skydrive-osgi-context.xml
 *         
 * @author fschoeppl
 *
 */
public class SkyDriveDescriptor implements SourceSinkDescribable {
	public static final String SKYDRIVE_ID = "org.backmeup.skydrive";
	
	public String getTitle() {
		return "SkyDrive";
	}

	@Override
	public String getImageURL() {
		return "http://3gontravel.com/wp-content/uploads/2011/12/Windows-Live-SkyDrive-Logo.png";
	}

	@Override
	public String getDescription() {
		return "Description for Skydrive Plugin";
	}

	@Override
	public String getId() {
		return SKYDRIVE_ID;
	}

	@Override
	public Type getType() {
		return Type.Sink;
	}  

}
