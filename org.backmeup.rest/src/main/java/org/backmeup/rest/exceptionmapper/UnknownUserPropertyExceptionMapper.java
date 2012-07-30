package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.UnknownUserPropertyException;
import org.backmeup.rest.data.ErrorEntity;

@Provider
public class UnknownUserPropertyExceptionMapper implements
		ExceptionMapper<UnknownUserPropertyException> {
	
	public Response toResponse(UnknownUserPropertyException uue) {
		return Response.status(Response.Status.NOT_FOUND).entity(new ErrorEntity(uue.getMessage())).build();
	}

}
