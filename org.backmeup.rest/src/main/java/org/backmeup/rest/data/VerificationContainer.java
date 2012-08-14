package org.backmeup.rest.data;

public class VerificationContainer {
  private String username;
  private String verificationKey;

  public VerificationContainer() {
    super();
  }

  public VerificationContainer(String username, String verificationKey) {
    super();
    this.username = username;
    this.verificationKey = verificationKey;
  }

  public String getUsername() {
    return username;
  }

  public String getVerificationKey() {
    return verificationKey;
  }

}
