package org.backmeup.skydrive;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;
import org.backmeup.skydrive.internal.SkyDriveSupport;
import org.backmeup.skydrive.internal.SkyDriveSupport.Entry;
import org.backmeup.skydrive.internal.SkyDriveSupport.Service;

public class SkyDriveDatasource extends FilesystemLikeDatasource {

	@Override
	public String getStatistics(Properties accesssData) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FilesystemURI> list(Properties accessData, FilesystemURI uri) {
		String path = uri == null ? "me/skydrive" : uri.toString();
		String mappedPath = uri == null ? "" : uri.getMappedUri().toString();
		List<FilesystemURI> uris = new ArrayList<FilesystemURI>();
		Service s = SkyDriveSupport.getService(accessData);
		for (Entry e : SkyDriveSupport.getFolderContent(s.service, s.accessToken, path)) {
			try {
				FilesystemURI furi = new FilesystemURI(new URI(e.getId()), e.isDirectory());
				furi.setMappedUri(new URI(mappedPath + "/" + e.getName()));
				uris.add(furi);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
		return uris;
	}

	@Override
	public InputStream getFile(Properties accessData, FilesystemURI uri) {
		Service s = SkyDriveSupport.getService(accessData);
		return SkyDriveSupport.getContentAsStream(s.service, s.accessToken, uri.toString());
	}

  @Override
  public List<String> getAvailableOptions(Properties accessData) {
    List<String> options = new ArrayList<String>();
    String path = "me/skydrive";        
    Service s = SkyDriveSupport.getService(accessData);
    for (Entry e : SkyDriveSupport.getFolderContent(s.service, s.accessToken, path)) {
      options.add(e.getName() + " (" + e.getId() + ")");      
    }
    return options;
  }	
}
