package org.backmeup.rest.provider;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Whenever a object will be converted into JSON,
 * it should not contain null values.
 * 
 * @author fschoeppl
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
	
	public ObjectMapper getContext(Class<?> clazz) {
		ObjectMapper result = new ObjectMapper();
		// do not export null properties into JSON, simply ignore them!
		result.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
		return result;
	}
	

}
