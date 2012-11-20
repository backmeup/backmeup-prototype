package org.backmeup.model.exceptions;

public class UnknownUserPropertyException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  private String property;

  public UnknownUserPropertyException(String property) {
    super("Unknown user property!");    
    this.setProperty(property);
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }
}
