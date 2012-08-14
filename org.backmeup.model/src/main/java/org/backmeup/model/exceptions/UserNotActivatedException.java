package org.backmeup.model.exceptions;

public class UserNotActivatedException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  private String username;

  public UserNotActivatedException(String username) {
    super("User " + username + " has not been activated!");
    this.username = username;
  }

  public String getUsername() {
    return username;
  }
}
