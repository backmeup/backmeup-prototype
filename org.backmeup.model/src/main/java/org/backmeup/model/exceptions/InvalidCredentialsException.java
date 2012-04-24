package org.backmeup.model.exceptions;


public class InvalidCredentialsException extends BackMeUpException{
	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException() {
		super("Invalid credentials");		
	}
}
