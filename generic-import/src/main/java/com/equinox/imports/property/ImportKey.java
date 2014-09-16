package com.equinox.imports.property;

import com.equinox.imports.ImportLineColumn;
import com.equinox.imports.exception.ImportPropertyException;

public class ImportKey extends AbstractImportField {

	private String id;

	public ImportKey() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public <T> T getValue(Class<T> type, ImportLineColumn column) throws ImportPropertyException {
		try {
			return getValue(type, column.getValue());
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
