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

@Provider
public class FirstTimeAuthAnswerJSONMessageBodyWriter implements MessageBodyWriter<PreAuthContainer> {
	
	public long getSize(PreAuthContainer arg0, Class<?> arg1, Type arg2, Annotation[] arg3,
			MediaType arg4) {
		return -1;
	}


	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3) {
		return arg0.equals(PreAuthContainer.class) && arg3.equals(MediaType.APPLICATION_JSON_TYPE);
	}


	public void writeTo(PreAuthContainer object, Class<?> arg1, Type arg2, Annotation[] arg3,
			MediaType arg4, MultivaluedMap<String, Object> arg5,
			OutputStream out) throws IOException, WebApplicationException {
		try {
			JSONObject obj = new JSONObject();
			obj.put("profileId", object.getProfileId());
			obj.put("type", object.getType());
			if (object.getRedirectURL() != null) {
				obj.put("redirectURL", object.getRedirectURL());			
			} else {
				JSONArray arr = new JSONArray();
				for (String input : object.getRequiredInputs()) {
					arr.put(input);
				}
				obj.put("requiredInputs", arr);
				JSONObject typeMapping = new JSONObject();
				for (Map.Entry<String, String> e : object.getTypeMapping().entrySet()) {
					typeMapping.put(e.getKey(), e.getValue());
				}
				obj.put("typeMapping", typeMapping);
			}
			String data = obj.toString();
			//The solidus issue: We don't escape solidus-characters. 
			data = data.replace("\\/", "/");
			out.write(data.getBytes("UTF-8"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
