package com.equinox.imports.exception;

import com.equinox.imports.property.ImportProperty;

public class InvalidFormatPropertyImportException extends ImportPropertyException {

	private static final long serialVersionUID = 765250736170890524L;

	public InvalidFormatPropertyImportException(ImportProperty property, String value) {
		super("Champ " + property.getName() + " au mauvais format [attendu : " + property.getType().getSimpleName()
				+ ", réel : " + value + "]");
	}

}
