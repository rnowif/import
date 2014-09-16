package com.equinox.imports.exception;

import com.equinox.imports.property.ImportProperty;

public class TooLongPropertyImportException extends ImportPropertyException {
	private static final long serialVersionUID = 765250736170890524L;

	public TooLongPropertyImportException(ImportProperty property, Integer actualSize) {
		super("Champ " + property.getName() + " trop long [attendu : " + property.getLength() + ", réel : "
				+ actualSize + "]");
	}
}
