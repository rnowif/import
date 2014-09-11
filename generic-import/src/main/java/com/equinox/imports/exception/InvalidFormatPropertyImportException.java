package com.equinox.imports.exception;

import com.equinox.imports.ImportLine;
import com.equinox.imports.property.ImportProperty;

public class InvalidFormatPropertyImportException extends ImportLineException {

	private static final long serialVersionUID = 765250736170890524L;

	public InvalidFormatPropertyImportException(ImportLine line, ImportProperty property, String value) {
		super(line, "Champ " + property.getName() + " au mauvais format [attendu : "
				+ property.getType().getSimpleName() + ", réel : " + value + "]");
	}

}
