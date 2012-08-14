package org.backmeup.model.exceptions;

public class EmailVerificationException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  
  public EmailVerificationException(String verificationKey) {
    super("Failed to verify key " + verificationKey);
  }

}
