package org.backmeup.plugin.api.connectors;

public class DatasourceException extends Exception {
	private static final long serialVersionUID = 1L;

	public DatasourceException(String msg) {
		super(msg);
	}
	
	public DatasourceException(Throwable t) {
		super(t);
	}
	
}
