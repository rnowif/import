package com.equinox.imports.exception;

public class TooLongPropertyImportException extends ImportPropertyException {
	private static final long serialVersionUID = 765250736170890524L;

	public TooLongPropertyImportException(String name, Integer maxSize, Integer actualSize) {
		super("Champ " + name + " trop long [attendu : " + maxSize + ", réel : " + actualSize + "]");
	}
}
