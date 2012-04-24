package org.backmeup.plugin.spi;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface InputBased extends Authorizable {
	
	public enum Type {
		String,
		Number,
		Password
	}
	
	public List<String> getRequiredInputFields();
	
	public Map<String, Type> getTypeMapping();
	
	public boolean isValid(Properties inputs);
	
}
