package org.backmeup.model.exceptions;

public class NotAnEmailAddressException extends BackMeUpException {
  private static final long serialVersionUID = 1L;
  
  private String expected;
  private String actual;
  
  public NotAnEmailAddressException(String expected, String actual) {
    super("The given email address \"" + actual + "\" is invalid! The regular expression that has been used is: \"" + expected +"\"");
    this.expected = expected;
    this.actual = actual;
  }

  public String getExpected() {
    return expected;
  }

  public String getActual() {
    return actual;
  }
}
