package org.backmeup.discmailing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;
import org.backmeup.plugin.spi.InputBased;

public class DiscmailingAuthenticator implements InputBased {
	private static final String PROP_FIRSTNAME = "Firstname";
	private static final String PROP_FIRSTNAME_DESC = "Firstname";
	private static final String PROP_SURNAME = "Surname";
	private static final String PROP_SURNAME_DESC = "Surname";
	private static final String PROP_STREET = "Street";
	private static final String PROP_STREET_DESC = "Street";
	private static final String PROP_CITY = "City";
	private static final String PROP_CITY_DESC = "City";
	private static final String PROP_POSTCODE = "Postcode";
	private static final String PROP_POSTCODE_DESC = "Postcode";
	
	@Override
	public AuthorizationType getAuthType() {
		return AuthorizationType.InputBased;
	}

	@Override
	public String postAuthorize(Properties inputProperties) {
		
		String ret_value = "";
		ret_value += inputProperties.getProperty(PROP_FIRSTNAME, PROP_FIRSTNAME);
		ret_value += " " + inputProperties.getProperty(PROP_SURNAME, PROP_SURNAME);
		ret_value += ", " +  inputProperties.getProperty(PROP_STREET, PROP_STREET);
		ret_value += ", " +  inputProperties.getProperty(PROP_CITY, PROP_CITY);
		
		return ret_value;
	}

	@Override
	public List<RequiredInputField> getRequiredInputFields() {
		List<RequiredInputField> inputs = new ArrayList<RequiredInputField>();
		
		inputs.add(new RequiredInputField (PROP_FIRSTNAME, PROP_FIRSTNAME, PROP_FIRSTNAME_DESC, true, 0, Type.String));
		inputs.add(new RequiredInputField (PROP_SURNAME, PROP_SURNAME, PROP_SURNAME_DESC, true, 1, Type.String));
		inputs.add(new RequiredInputField (PROP_STREET, PROP_STREET, PROP_STREET_DESC, true, 2, Type.String));
		inputs.add(new RequiredInputField (PROP_CITY, PROP_CITY, PROP_CITY_DESC, true, 3, Type.String));
		inputs.add(new RequiredInputField (PROP_POSTCODE, PROP_POSTCODE, PROP_POSTCODE_DESC, true, 4, Type.String));
		
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
