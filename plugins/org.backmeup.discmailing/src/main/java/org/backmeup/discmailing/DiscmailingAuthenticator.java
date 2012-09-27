package org.backmeup.discmailing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.plugin.spi.InputBased;

public class DiscmailingAuthenticator implements InputBased {
	private static final String PROP_FIRSTNAME = "Firstname";
	private static final String PROP_SURNAME = "Surname";
	private static final String PROP_STREET = "Street";
	private static final String PROP_CITY = "City";
	private static final String PROP_POSTCODE = "Postcode";
	
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.InputBased;
	}

	@Override
	public void postAuthorize(Properties inputProperties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getRequiredInputFields() {
		List<String> inputs = new ArrayList<String>();
		inputs.add(PROP_FIRSTNAME);
		inputs.add(PROP_SURNAME);
	    inputs.add(PROP_STREET);
	    inputs.add(PROP_CITY);
	    inputs.add(PROP_POSTCODE);
	    return inputs;
	}

	@Override
	public Map<String, Type> getTypeMapping() {
		Map<String, Type> typeMapping = new HashMap<String, Type>();
		typeMapping.put(PROP_FIRSTNAME, Type.String);
		typeMapping.put(PROP_SURNAME, Type.String);
	    typeMapping.put(PROP_STREET, Type.String);
	    typeMapping.put(PROP_CITY, Type.String);
	    typeMapping.put(PROP_POSTCODE, Type.String); 
	    return typeMapping;
	}

	@Override
	public boolean isValid(Properties inputs) {
		return true;
	}
}
