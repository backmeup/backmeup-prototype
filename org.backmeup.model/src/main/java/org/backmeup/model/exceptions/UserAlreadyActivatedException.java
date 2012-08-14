package org.backmeup.model.exceptions;

public class UserAlreadyActivatedException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  private String username;

  public UserAlreadyActivatedException(String username) {
    super("User " + username
        + " is active and does not need a new verification email!");
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
