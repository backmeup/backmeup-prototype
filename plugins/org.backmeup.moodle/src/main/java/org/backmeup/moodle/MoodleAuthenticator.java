package org.backmeup.moodle;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
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
	public String postAuthorize(Properties inputProperties) {
	  // TODO return the username of the moodle account
	  return inputProperties.getProperty("Username");
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> requiredFields = new LinkedList<RequiredInputField>();
		
		requiredFields.add(new RequiredInputField ("Username", "Username", "Username", true, 0, Type.String));
		requiredFields.add(new RequiredInputField ("Password", "Password", "Password", true, 1, Type.Password));
		requiredFields.add(new RequiredInputField ("Moodle Server Url", "Moodle Server Url", "Moodle Server Url", true, 2, Type.String));

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
					+ "blocks/backmeup/service.php?username=" + username
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
			return false;
		}
	}

}
