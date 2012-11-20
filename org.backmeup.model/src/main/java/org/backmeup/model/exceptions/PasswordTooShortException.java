package org.backmeup.model.exceptions;

public class PasswordTooShortException extends BackMeUpException{
  private static final long serialVersionUID = 1L;
  
  private int minimalLength;
  
  private int actualLength;
  
  public PasswordTooShortException(int minimalLength, int actualLength) {
    super("Password too short!");
    this.minimalLength = minimalLength;
    this.actualLength = actualLength;
  } 

  public int getMinimalLength() {
    return minimalLength;
  }

  public int getActualLength() {
    return actualLength;
  }
}
