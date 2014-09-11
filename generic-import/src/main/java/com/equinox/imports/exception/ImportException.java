package com.equinox.imports.exception;

public class ImportException extends Exception {

	private static final long serialVersionUID = -3026903122577506560L;

	public ImportException(String message) {
		super(message);
	}

	public ImportException(String message, Throwable e) {
		super(message, e);
	}

}
