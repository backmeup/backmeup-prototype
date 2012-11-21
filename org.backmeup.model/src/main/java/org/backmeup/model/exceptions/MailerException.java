package org.backmeup.model.exceptions;

public class MailerException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  private String to;
  private String subject;
  private String message;
  private String mimeType;
  
  public MailerException(String to, String subject, String message, String mimeType, Exception innerException) {
    super("Failed to send email!", innerException);
    this.to = to;
    this.message = message;
    this.mimeType = mimeType;
    this.subject = subject;
  }

  public String getTo() {
    return to;
  }

  public String getSubject() {
    return subject;
  }

  public String getMessage() {
    return message;
  }

  public String getMimeType() {
    return mimeType;
  }
}
