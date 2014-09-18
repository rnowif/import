package com.equinox.imports.exception;

public class InvalidFormatPropertyImportException extends ImportPropertyException {

	private static final long serialVersionUID = 765250736170890524L;

	public InvalidFormatPropertyImportException(String name, String expected, String real) {
		this(name, expected, real, null);
	}

	public InvalidFormatPropertyImportException(String name, String expected, String real, Exception e) {
		super("Champ " + name + " au mauvais format [attendu : " + expected + ", réel : " + real + "]", e);
	}

}
