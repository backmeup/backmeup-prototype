package org.backmeup.skydrive;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;

import org.backmeup.plugin.api.Metainfo;
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
	public List<FilesystemURI> list(Properties accessData, List<String> options, FilesystemURI uri) {
		String path = uri == null ? "me/skydrive" : uri.toString();
		String mappedPath = uri == null ? "" : uri.getMappedUri().toString();
		List<FilesystemURI> uris = new ArrayList<FilesystemURI>();
		Service s = SkyDriveSupport.getService(accessData);
		for (Entry e : SkyDriveSupport.getFolderContent(s.service, s.accessToken, path)) {
			try {
				FilesystemURI furi = new FilesystemURI(new URI(e.getId()), e.isDirectory());
				Metainfo metainfo = new Metainfo();
				metainfo.setBackupDate(new Date());
				metainfo.setDestination(e.getName());
				metainfo.setSource("skydrive");
				metainfo.setCreated(e.getCreated());
				metainfo.setModified(e.getModified());
				metainfo.setParent(path);
				metainfo.setType(e.isDirectory() ? "directory" : new MimetypesFileTypeMap().getContentType(e.getName()));				
				furi.setMappedUri(new URI(mappedPath + "/" + e.getName()));
				furi.addMetainfo(metainfo);
				if (!path.equals("me/skydrive") || options.contains(e.getName()))
				  uris.add(furi);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
		return uris;
	}

	@Override
	public InputStream getFile(Properties accessData, List<String> options, FilesystemURI uri) {
		Service s = SkyDriveSupport.getService(accessData);
		return SkyDriveSupport.getContentAsStream(s.service, s.accessToken, uri.toString());
	}

  @Override
  public List<String> getAvailableOptions(Properties accessData) {
    List<String> options = new ArrayList<String>();
    String path = "me/skydrive";        
    Service s = SkyDriveSupport.getService(accessData);
    for (Entry e : SkyDriveSupport.getFolderContent(s.service, s.accessToken, path)) {
      options.add(e.getName());      
    }
    return options;
  }	
}
