package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.rest.data.ErrorEntity;

public class BackMeUpExceptionMapper implements
    ExceptionMapper<BackMeUpException> {

  public Response toResponse(BackMeUpException are) {    
    return Response.status(Status.BAD_REQUEST)
        .entity(new ErrorEntity(are.getClass().getName(), are.getMessage())).build();
  }

}
