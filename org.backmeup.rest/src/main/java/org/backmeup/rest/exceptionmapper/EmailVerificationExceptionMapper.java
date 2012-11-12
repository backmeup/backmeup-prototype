package org.backmeup.rest.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.backmeup.model.exceptions.EmailVerificationException;
import org.backmeup.rest.data.ErrorEntity;

public class EmailVerificationExceptionMapper implements
  ExceptionMapper<EmailVerificationException>{

  @Override
  public Response toResponse(EmailVerificationException exception) {
    return Response.status(Status.BAD_REQUEST)
        .entity(new EmailErrorEntity(exception)).build();    
  }

  public static class EmailErrorEntity extends ErrorEntity {
    private String verificationKey;

    public EmailErrorEntity(EmailVerificationException eve) {
      super(eve.getClass().getName(), eve);
      this.verificationKey = eve.getVerificationKey();
    }

    public String getVerificationKey() {
      return verificationKey;
    }
  }
}
