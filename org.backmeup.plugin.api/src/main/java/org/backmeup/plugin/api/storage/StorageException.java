package org.backmeup.plugin.api.storage;

public class StorageException extends Exception {
	private static final long serialVersionUID = 1L;

	public StorageException(String msg) {
		super(msg);
	}
	
	public StorageException(Throwable t) {
		super(t);
	}

}
