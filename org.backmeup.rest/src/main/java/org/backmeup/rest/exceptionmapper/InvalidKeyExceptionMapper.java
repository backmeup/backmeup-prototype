package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.InvalidKeyException;
import org.backmeup.rest.data.ErrorEntity;

public class InvalidKeyExceptionMapper implements
ExceptionMapper<InvalidKeyException> {

  @Override
  public Response toResponse(InvalidKeyException exception) {
    return Response.status(Response.Status.UNAUTHORIZED).entity(new ErrorEntity(InvalidKeyException.class.getName(), exception)).build();    
  }
}
