package org.backmeup.model.exceptions;


public class UnknownUserException extends BackMeUpException {
	private static final long serialVersionUID = 1L;

	public UnknownUserException(String username) {
		super("Unknown user " + username);
	}
}
