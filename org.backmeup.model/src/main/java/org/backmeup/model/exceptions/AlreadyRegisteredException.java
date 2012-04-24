package org.backmeup.model.exceptions;


public class AlreadyRegisteredException extends BackMeUpException {
	private static final long serialVersionUID = 1L;
	private String username;
	
	public AlreadyRegisteredException(String username) {
		super(String.format("The user %s has already been registered!", username));
		this.username = username;
		
	}

	public String getUsername() {
		return username;
	}
}
