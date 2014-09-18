package com.equinox.imports.property;

import com.equinox.imports.file.ImportFile;

public class ComponentImportProperty {

	private ImportFile file;
	private String name;
	private String columnIndex;
	private String defaultValue;

	public ComponentImportProperty() {
	}

	public ImportFile getFile() {
		return file;
	}

	public void setFile(ImportFile file) {
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(String columnIndex) {
		this.columnIndex = columnIndex;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

}
