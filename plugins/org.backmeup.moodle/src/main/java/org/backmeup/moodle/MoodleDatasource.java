package org.backmeup.moodle;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.backmeup.plugin.api.connectors.FilesystemLikeDatasource;
import org.backmeup.plugin.api.connectors.FilesystemURI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * This class lists all available files on the Moodle server, normally one
 * *.zip per course, and downloads them.
 * 
 * @author florianjungwirth
 *
 */
public class MoodleDatasource extends FilesystemLikeDatasource {

	@Override
	public String getStatistics(Properties items) {
		return null;
	}

	@Override
	public InputStream getFile(Properties items, FilesystemURI uri) {
		try {
			return uri.getUri().toURL().openStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<FilesystemURI> list(Properties items, FilesystemURI uri) {

		List<FilesystemURI> results = new ArrayList<FilesystemURI>();

		String serverurl = items.getProperty("Moodle Server Url");
		String username = items.getProperty("Username");
		String password = items.getProperty("Password");

		serverurl = serverurl.endsWith("/") ? serverurl : serverurl+"/";
		
		try {
			String authUrl = serverurl
					+ "blocks/backmeup/service.php?username=" + username
					+ "&password=" + password + "&action=list";
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			Document doc = docBuilder.parse(authUrl);
			NodeList nodes = doc.getElementsByTagName("course");

			for (int i = 0; i < nodes.getLength(); i++) {
				String fileuri = nodes.item(i).getLastChild().getTextContent();
				FilesystemURI filesystemUri = new FilesystemURI(new URI(fileuri), false);
				filesystemUri.setMappedUri(new URI(new File(fileuri).getName())); // just us the name of the file as its destination
				results.add(filesystemUri);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

}
