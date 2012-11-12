package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.rest.data.ErrorEntity;

@Provider
public class UnknownUserExceptionMapper implements
		ExceptionMapper<UnknownUserException> {
	
	public Response toResponse(UnknownUserException uue) {
		return Response.status(Response.Status.NOT_FOUND).entity(new UnknownUserEntity(uue)).build();
	}
	
	public static class UnknownUserEntity extends ErrorEntity {
    private String username;

    public UnknownUserEntity(UnknownUserException eve) {
      super(eve.getClass().getName(), eve);
      this.username = eve.getUsername();
    }

    public String getUsername() {
      return username;
    }
  }
}
