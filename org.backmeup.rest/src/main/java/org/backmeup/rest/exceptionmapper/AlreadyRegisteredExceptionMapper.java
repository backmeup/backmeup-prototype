package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.AlreadyRegisteredException;
import org.backmeup.rest.data.ErrorEntity;

@Provider
public class AlreadyRegisteredExceptionMapper implements
		ExceptionMapper<AlreadyRegisteredException> {
	
	public Response toResponse(AlreadyRegisteredException are) {
		return Response.status(Status.BAD_REQUEST).entity(new ErrorEntity(are.getMessage())).build();
	}

}
