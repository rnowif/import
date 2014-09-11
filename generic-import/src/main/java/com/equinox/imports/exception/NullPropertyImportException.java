package com.equinox.imports.exception;

import com.equinox.imports.ImportLine;
import com.equinox.imports.property.ImportProperty;

public class NullPropertyImportException extends ImportLineException {

	private static final long serialVersionUID = 765250736170890524L;

	public NullPropertyImportException(ImportLine line, ImportProperty property) {
		super(line, "Champ " + property.getName() + " vide");
	}
}
