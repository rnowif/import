package com.equinox.imports.exception;

public class ImportPropertyException extends ImportException {

	private static final long serialVersionUID = 1L;

	public ImportPropertyException(String message) {
		super(message);
	}

	public ImportPropertyException(String message, Throwable e) {
		super(message, e);
	}

}
