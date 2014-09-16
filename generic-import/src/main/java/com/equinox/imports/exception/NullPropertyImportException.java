package com.equinox.imports.exception;

import com.equinox.imports.property.ImportProperty;
import com.equinox.imports.property.SubClassImportProperty;

public class NullPropertyImportException extends ImportPropertyException {

	private static final long serialVersionUID = 765250736170890524L;

	public NullPropertyImportException(ImportProperty property) {
		super("Champ " + property.getName() + " vide");
	}

	public NullPropertyImportException(SubClassImportProperty subClass) {
		super("Sous classe " + subClass.getName() + " vide");
	}
}
