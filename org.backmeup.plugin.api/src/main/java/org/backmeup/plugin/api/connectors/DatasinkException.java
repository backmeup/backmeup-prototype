package org.backmeup.plugin.api.connectors;

public class DatasinkException extends Exception {
	private static final long serialVersionUID = 1L;

	public DatasinkException(String msg) {
		super(msg);
	}
	
	public DatasinkException(Throwable t) {
		super(t);
	}

}
