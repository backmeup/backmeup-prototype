package org.backmeup.model.exceptions;

import org.backmeup.model.spi.ValidationExceptionType;



/**
 * When a plugin returns false when calling
 * InputBased#isValid, this exception will be thrown
 * by the BusinessLogic.
 * 
 * @author fschoeppl
 *
 */
public class ValidationException extends BackMeUpException {
	private static final long serialVersionUID = 1L;
	
  private ValidationExceptionType type;
	
	public ValidationException(ValidationExceptionType type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	public ValidationException(ValidationExceptionType type, String message) {
		this(type, message, null);
	}

  public ValidationExceptionType getType() {
    return type;
  } 
}
