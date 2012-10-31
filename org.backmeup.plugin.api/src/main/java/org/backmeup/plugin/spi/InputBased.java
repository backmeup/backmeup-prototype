package org.backmeup.plugin.spi;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.api.RequiredInputField;
import org.backmeup.model.api.RequiredInputField.Type;

public interface InputBased extends Authorizable {
	
	public List<RequiredInputField> getRequiredInputFields();
	
	public Map<String, Type> getTypeMapping();
	
	public boolean isValid(Properties inputs);
	
}
