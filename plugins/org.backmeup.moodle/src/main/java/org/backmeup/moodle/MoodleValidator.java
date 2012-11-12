package org.backmeup.moodle;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.backmeup.model.ValidationNotes;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.model.spi.Validationable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class validates the stored access data of a moodle profile.
 * 
 * @author florianjungwirth
 * 
 */
public class MoodleValidator implements Validationable {

	@Override
	public ValidationNotes validate(Properties accessData) {
		ValidationNotes notes = new ValidationNotes();
		String serverurl = accessData.getProperty("Moodle Server Url");
		String username = accessData.getProperty("Username");
		String password = accessData.getProperty("Password");

		serverurl = serverurl.endsWith("/") ? serverurl : serverurl + "/";
		String authurl = serverurl + "blocks/backmeup/service.php?username="
				+ username + "&password=" + password + "&action=auth";

		try {
			// 1. Check if the Moodle server can be reached
			if (getResponseCode(serverurl) >= 400) {			  
				notes.addValidationEntry(ValidationExceptionType.APIException,
						MoodleDescriptor.MOODLE_ID, new Exception("Server cannot be achieved!"));
			}
			// 2. Check if the server-side Moodle plugin is installed
			else if (getResponseCode(authurl) >= 400) {
				notes.addValidationEntry(ValidationExceptionType.APIException,
				    MoodleDescriptor.MOODLE_ID, new Exception("The server-side Moodle Plugin is not installed!"));
			} else {
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory
						.newDocumentBuilder();

				Document doc = docBuilder.parse(authurl);
				NodeList nodes = doc.getElementsByTagName("result");
				Element result = (Element) nodes.item(0);
				// 3. Check if the login data is correct
				if (result.getTextContent().compareTo("true") != 0)
					notes.addValidationEntry(
							ValidationExceptionType.AuthException,
							MoodleDescriptor.MOODLE_ID, new Exception("Invalid credentials!"));
				else {
					authurl = serverurl
							+ "blocks/backmeup/service.php?username="
							+ username + "&password=" + password
							+ "&action=list";

					doc = docBuilder.parse(authurl);
					nodes = doc.getElementsByTagName("course");
					// 4. Check if there are any courses to backup
					if (nodes.getLength() == 0)
						notes.addValidationEntry(
								ValidationExceptionType.AuthException,
								MoodleDescriptor.MOODLE_ID,
								new Exception("Login was successfull, but the user is not enrolled in any course, so there's no data to backup!"));
				}
			}
		} catch (MalformedURLException e) {
			notes.addValidationEntry(
					ValidationExceptionType.AuthException,
					MoodleDescriptor.MOODLE_ID,
					e);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return notes;
	}

	private int getResponseCode(String urlString) throws MalformedURLException,
			IOException {
		try {
			URL u = new URL(urlString);
			HttpURLConnection huc = (HttpURLConnection) u.openConnection();
			huc.setRequestMethod("GET");
			huc.connect();
			return huc.getResponseCode();
		} catch (Exception e) {
			// Problem accured, return 400 error
			return 400;
		}
	}

}
