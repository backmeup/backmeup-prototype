package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.backmeup.rest.data.ErrorEntity;

@Provider
public class IllegalArgumentExceptionMapper implements
		ExceptionMapper<IllegalArgumentException> {
	
	public Response toResponse(IllegalArgumentException arg0) {
		return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorEntity(arg0.getMessage())).build();
	}

}
