package org.backmeup.rest.bodywriters;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.backmeup.rest.data.PreAuthContainer;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 * This class converts 
 * the PreAuthContainer class into
 * JSON.
 * 
 * @author fschoeppl
 *
 */
@Provider
public class FirstTimeAuthAnswerJSONMessageBodyWriter implements MessageBodyWriter<PreAuthContainer> {
	
	@Override
	public long getSize(PreAuthContainer t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	} 

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return type.equals(PreAuthContainer.class) && mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
	}

	@Override
	public void writeTo(PreAuthContainer t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		try {
			JSONObject obj = new JSONObject();
			obj.put("profileId", t.getProfileId());
			obj.put("type", t.getType());
			if (t.getRedirectURL() != null) {
				obj.put("redirectURL", t.getRedirectURL());			
			} else {
				JSONArray arr = new JSONArray();
				for (String input : t.getRequiredInputs()) {
					arr.put(input);
				}
				obj.put("requiredInputs", arr);
				JSONObject typeMapping = new JSONObject();
				for (Map.Entry<String, String> e : t.getTypeMapping().entrySet()) {
					typeMapping.put(e.getKey(), e.getValue());
				}
				obj.put("typeMapping", typeMapping);
			}
			String data = obj.toString();
			//The solidus issue: We don't escape solidus-characters. 
			data = data.replace("\\/", "/");
			entityStream.write(data.getBytes("UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
}
