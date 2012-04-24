package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.InvalidCredentialsException;

@Provider
public class InvalidCredentialsMapper implements ExceptionMapper<InvalidCredentialsException> {
	
	public Response toResponse(InvalidCredentialsException arg0) {
		return Response.status(Response.Status.UNAUTHORIZED).build();
	}

}
