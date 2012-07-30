package org.backmeup.model.exceptions;

public class UnknownUserPropertyException extends BackMeUpException {
  private static final long serialVersionUID = 1L;

  public UnknownUserPropertyException(String property) {
    super("Unknown user property: " + property);
  }
}
