package org.backmeup.plugin.api.actions;

public class ActionException extends Exception {

	private static final long serialVersionUID = 5407871689527845897L;

	public ActionException (String msg) {
		super(msg);
	}
	
	public ActionException (Throwable t) {
		super(t);
	}

}
