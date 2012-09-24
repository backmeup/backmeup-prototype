package org.backmeup.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.backmeup.model.exceptions.MailerException;
import org.backmeup.utilities.mail.Mailer;

@Path("mails")
public class Mails {
  @POST
  @Path("/send/text")   
  @Produces("application/json")
  public void sendEmail(@FormParam("to") String to, @FormParam("subject") String subject, @FormParam("message") String message) {
    try {
      Mailer.synchronousSend(to, subject, message, "text/plain");
    } catch (Exception e) {
      throw new MailerException(to, subject, message, "text/plain", e);
    }
  }
  
  @POST
  @Path("/send/html")   
  @Produces("application/json")
  public void sendHtmlEmail(@FormParam("to") String to, @FormParam("subject") String subject, @FormParam("message") String message) {
    try {
      Mailer.synchronousSend(to, subject, message, "text/html");
    } catch (Exception e) {
      throw new MailerException(to, subject, message, "text/html", e);
    }
  }
}
