package org.backmeup.model.exceptions;

public class UserAlreadyActivatedException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  private String username;

  public UserAlreadyActivatedException(String username) {
    super("User is already active!");
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
