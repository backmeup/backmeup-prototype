package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.UserNotActivatedException;
import org.backmeup.rest.data.ErrorEntity;

public class UserNotActivatedExceptionMapper implements
    ExceptionMapper<UserNotActivatedException> {

  @Override
  public Response toResponse(UserNotActivatedException exception) {
    return Response.status(Status.BAD_REQUEST)
        .entity(new UserNotActivatedEntity(exception)).build();    
  }

  public static class UserNotActivatedEntity extends ErrorEntity {
    private String username;

    public UserNotActivatedEntity(UserNotActivatedException eve) {
      super(eve.getClass().getName(), eve);
      this.username = eve.getUsername();
    }

    public String getUsername() {
      return username;
    }
  }

}
