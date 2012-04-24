package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NullPointerExceptionMapper implements
		ExceptionMapper<NullPointerException> {
	
	public Response toResponse(NullPointerException arg0) {
		return Response.status(Status.BAD_REQUEST).build();
	}

}
