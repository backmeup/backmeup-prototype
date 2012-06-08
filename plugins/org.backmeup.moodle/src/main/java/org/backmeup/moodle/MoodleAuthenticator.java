package org.backmeup.moodle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.backmeup.plugin.spi.InputBased;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class defines the required user input fields and validates
 * the input data.
 * 
 * @author florianjungwirth
 *
 */
public class MoodleAuthenticator implements InputBased {

	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.InputBased;
	}

	@Override
	public void postAuthorize(Properties inputProperties) {

	}

	@Override
	public List<String> getRequiredInputFields() {
		List<String> requiredFields = new LinkedList<String>();
		requiredFields.add("Username");
		requiredFields.add("Password");
		requiredFields.add("Moodle Server Url");

		return requiredFields;
	}

	@Override
	public Map<String, Type> getTypeMapping() {
		Map<String, Type> mapping = new TreeMap<String, Type>();
		mapping.put("Username", Type.String);
		mapping.put("Password", Type.Password);
		mapping.put("Moodle Server Url", Type.String);

		return mapping;
	}

	@Override
	public boolean isValid(Properties inputProperties) {
		String serverurl = inputProperties.getProperty("Moodle Server Url");
		String username = inputProperties.getProperty("Username");
		String password = inputProperties.getProperty("Password");
		
		serverurl = serverurl.endsWith("/") ? serverurl : serverurl+"/";
		
		try {
			String authUrl = serverurl
					+ "blocks/exaport/bmu_valid.php?username=" + username
					+ "&password=" + password + "&action=auth";
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

			Document doc = docBuilder.parse(authUrl);
			NodeList nodes = doc.getElementsByTagName("result");
			Element result = (Element) nodes.item(0);
			if (result.getTextContent().compareTo("true") == 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
