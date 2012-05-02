package org.backmeup.dropbox;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxDatasource extends FilesystemLikeDatasource {
	
	public static final String PROPERTY_TOKEN = "token";
		
	public static final String PROPERTY_SECRET = "secret";
	
	public DropboxDatasource() {
		
	}
	
	private DropboxAPI<WebAuthSession> getApi(Properties items) {
		String token = items.getProperty(PROPERTY_TOKEN);
		String secret = items.getProperty(PROPERTY_SECRET);
		System.out.println("Getting WebAuthSession...");
		WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();
		session.setAccessTokenPair(new AccessTokenPair(token, secret));
		System.out.println("Set accessTokens!... token = " + token + " secret = " + secret);
		System.out.println("AppKeys: key = " + session.getAppKeyPair().key + " secret = " + session.getAppKeyPair().secret);
		
		// TODO throw an exception on fail
		session.isLinked();
		System.out.println("isLinked() ? " + session.isLinked());
		return new DropboxAPI<WebAuthSession>(session);
	}

	@Override
	public List<FilesystemURI> list(Properties items, FilesystemURI uri) {
		String path = (uri == null) ? "/" : uri.toString();
		DropboxAPI<WebAuthSession> api = getApi(items);
		List<FilesystemURI> uris = new ArrayList<FilesystemURI>();
		
		try {
			System.out.println("Listing files... " + path);
			Entry entry = api.metadata(path, 100, null, true, null);
			System.out.format("Entry: \n\t%s\n\t%s\n\t%s\n", entry, entry.isDir, entry.path);
			for (Entry e : entry.contents) {
				uris.add(new FilesystemURI(new URI(e.path), e.isDir));
				System.out.println("Adding " + e.path);
			}
		} catch (DropboxException e) {
			// TODO Create human-readable error output in view
		} catch (URISyntaxException e) {
			// TODO Create human-readable error output in view
		}
		System.out.println("Returning uris...");
		return uris;
	}

	@Override
	public InputStream getFile(Properties items, FilesystemURI uri) {
		try {
			System.out.println("Downloading from uri " + uri.toString());
			return getApi(items).getFileStream(uri.toString(), null);
		} catch (DropboxException e) {
			// TODO Create human-readable error output in view
		}
		return null;
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
