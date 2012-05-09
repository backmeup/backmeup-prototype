package org.backmeup.model.exceptions;

/**
 * The root of all exceptions 
 * that occur in the BackMeUp core.
 * 
 * @author fschoeppl
 *
 */
public class BackMeUpException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public BackMeUpException(String message) {
		super(message);
	}

	public BackMeUpException() {
		super();
	}

	public BackMeUpException(String message, Throwable cause) {
		super(message, cause);
	}

	public BackMeUpException(Throwable cause) {
		super(cause);
	}
}
