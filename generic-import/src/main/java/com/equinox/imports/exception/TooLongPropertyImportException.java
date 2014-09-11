package com.equinox.imports.exception;

import com.equinox.imports.ImportLine;
import com.equinox.imports.property.ImportProperty;

public class TooLongPropertyImportException extends ImportLineException {
	private static final long serialVersionUID = 765250736170890524L;

	public TooLongPropertyImportException(ImportLine line, ImportProperty property, Integer actualSize) {
		super(line, "Champ " + property.getName() + " trop long [attendu : " + property.getLength() + ", réel : "
				+ actualSize + "]");
	}
}
