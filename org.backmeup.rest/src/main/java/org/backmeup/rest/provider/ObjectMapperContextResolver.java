package org.backmeup.rest.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
	
	public ObjectMapper getContext(Class<?> clazz) {
		ObjectMapper result = new ObjectMapper();
		result.setSerializationInclusion(Inclusion.NON_NULL);
		return result;
	}
	

}
