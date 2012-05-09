package org.backmeup.model.exceptions;


/**
 * If the credentials are invalid, this exception
 * will be thrown. 
 * @author fschoeppl
 *
 */
public class InvalidCredentialsException extends BackMeUpException{
	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException() {
		super("Invalid credentials");		
	}
}
