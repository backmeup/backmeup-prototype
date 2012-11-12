package org.backmeup.model.exceptions;

public class EmailVerificationException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  
  private String verificationKey;
  
  public EmailVerificationException(String verificationKey) {
    super("Failed to verify email");
    this.verificationKey = verificationKey;
  }

  public String getVerificationKey() {
    return verificationKey;
  }

}
