package org.backmeup.skydrive;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.backmeup.model.spi.SourceSinkDescribable;
import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;
import org.backmeup.skydrive.internal.SkyDriveSupport;
import org.backmeup.skydrive.internal.SkyDriveSupport.Service;

public class SkyDriveDatasource extends FilesystemLikeDatasource implements SourceSinkDescribable {

	public SkyDriveDatasource() {
	}

	public String getTitle() {
		return "SkyDrive";
	}

	@Override
	public List<FilesystemURI> list(Properties accessData, FilesystemURI uri) {
		Service s = SkyDriveSupport.getService(accessData);
		System.out.println("Listing " + accessData + " \nURI: " + uri);
		return null;
	}

	@Override
	public InputStream getFile(Properties accessData, FilesystemURI uri) { 
		System.out.println("getFile " + accessData + " \nURI: " + uri);
		return null;
	}

	@Override
	public String getStatistics(Properties accessData) {
		System.out.println("getStatistics " + accessData);
		return null;
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
		return "org.backmeup.skydrive";
	}

	@Override
	public Type getType() {
		return Type.Sink;
	}  
}
