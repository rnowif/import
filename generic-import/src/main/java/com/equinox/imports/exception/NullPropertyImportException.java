package com.equinox.imports.exception;


public class NullPropertyImportException extends ImportPropertyException {

	private static final long serialVersionUID = 765250736170890524L;

	public NullPropertyImportException(String name) {
		super("Champ " + name + " vide");
	}

}
