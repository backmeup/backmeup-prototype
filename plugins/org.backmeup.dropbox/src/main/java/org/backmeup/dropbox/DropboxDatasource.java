package org.backmeup.dropbox;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.Metainfo;
import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.WebAuthSession;

/**
 * The DropboxDatasource is capable of listing all directories
 * and files of a certain directory and of downloading certain
 * files from Dropbox.
 * 
 * @author fschoeppl
 */
public class DropboxDatasource extends FilesystemLikeDatasource {	
	private static final String DROPBOX = "dropbox";
	private static final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US); 
	
	
	@Override
	public List<FilesystemURI> list(Properties items, FilesystemURI uri) {
		String path = (uri == null) ? "/" : uri.toString();
		DropboxAPI<WebAuthSession> api = DropboxHelper.getApi(items);
		List<FilesystemURI> uris = new ArrayList<FilesystemURI>();
		
		try {
			path = path.replace("%20", " "); // Dropbox cannot handle %20 encoded spaces, but URI needs it
			Entry entry = api.metadata(path, 100, null, true, null);				
			for (Entry e : entry.contents) {
				String encodedURI = e.path.replace(" ", "%20");
				FilesystemURI furi = new FilesystemURI(new URI(encodedURI), e.isDir);
				Metainfo meta = new Metainfo();
				meta.setId(encodedURI);
				if (uri != null)
					meta.setParent(uri.getMetainfo().getId());
				meta.setModified(formatter.parse(e.modified));
				meta.setBackupDate(new Date());
				meta.setDestination(e.path);
				meta.setSource(DROPBOX);
				meta.setType(e.isDir ? "directory" : new MimetypesFileTypeMap().getContentType(e.path));
				furi.setMetainfo(meta);
				uris.add(furi);
			}
		} catch (DropboxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, String.format("Exception while metadata call with folder parameter %s, limit 100", path), e);
		} catch (URISyntaxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, String.format("URISyntaxException while creating FilesystemURIs with name %s", e.getInput()), e);
		} catch (ParseException e) { 
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, String.format("ParseException during date parse process \"%s\"", e.getMessage()), e);
		} 
		return uris;
	}

	@Override
	public InputStream getFile(Properties items, FilesystemURI uri) {
		String path = "";
		try {
			path = uri.toString().replace("%20", " ");
			return DropboxHelper.getApi(items).getFileStream(path, null);
		} catch (DropboxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, String.format("Error downloading file \" %s\"", path), e);
		}
	}

	@Override
	public String getStatistics(Properties items) {
		StringBuffer html = new StringBuffer();
		html.append("<ul>");
		for (FilesystemURI uri : list(items)) {
			html.append("<li>" + uri.toString() + "</li>");
		}
		html.append("</ul>");
		return html.toString();
	} 
}
