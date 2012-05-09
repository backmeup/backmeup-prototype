package org.backmeup.model.exceptions;

/**
 * An operation might fail because the given 
 * user is unknown to the system. This exception
 * should be thrown then.
 * 
 * @author fschoeppl
 *
 */
public class UnknownUserException extends BackMeUpException {
	private static final long serialVersionUID = 1L;

	public UnknownUserException(String username) {
		super("Unknown user " + username);
	}
}
