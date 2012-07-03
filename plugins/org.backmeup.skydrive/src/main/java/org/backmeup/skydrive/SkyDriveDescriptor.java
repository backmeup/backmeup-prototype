package org.backmeup.skydrive;

import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.Metadata;
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
		return Type.Both;
	}

	@Override
	public Properties getMetadata(Properties accessData) {
		Properties metadata = new Properties();
		metadata.setProperty(Metadata.BACKUP_FREQUENCY, "daily");
		metadata.setProperty(Metadata.FILE_SIZE_LIMIT, "150");
		
		try {
			//Service api = SkyDriveSupport.getService(accessData);			
			//TODO: Implement http://msdn.microsoft.com/en-us/library/live/hh826545.aspx#quota to get the maximal amount of data that can be stored
			//      on the users skydrive account + the total quota that is available
			//      SkyDriveSupport.getAccountInformation() -> account.currentQuota + account.freeQuota
						
			// A new account has a maximum of 7GB free space 
			metadata.setProperty(Metadata.QUOTA_LIMIT, "7000");
		} catch (Exception ex) {
			ex.printStackTrace();
		}		
		return metadata;
	}  

}
