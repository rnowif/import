package com.equinox.imports.exception;

import com.equinox.imports.ImportLine;

public class ImportLineException extends ImportException {

	private static final long serialVersionUID = 2037611976507043163L;

	public ImportLineException(ImportLine line, String message) {
		super("Erreur : ligne " + line.getNumber() + " rejetée.\tCause : " + message);
	}

}
