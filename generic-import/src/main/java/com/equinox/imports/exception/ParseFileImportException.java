package com.equinox.imports.exception;

import com.equinox.imports.file.ImportFile;

public class ParseFileImportException extends ImportException {

	private static final long serialVersionUID = 765250736170890524L;

	public ParseFileImportException(String message, ImportFile file, Throwable e) {
		super("Erreur lors du parsage du fichier " + file.getId() + " : " + message, e);
	}

	public ParseFileImportException(String message, ImportFile file) {
		this(message, file, null);
	}

}
