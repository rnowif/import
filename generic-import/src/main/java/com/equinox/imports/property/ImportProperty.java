package com.equinox.imports.property;

import com.equinox.imports.ImportLineColumn;
import com.equinox.imports.exception.ImportPropertyException;
import com.equinox.imports.exception.InvalidFormatPropertyImportException;
import com.equinox.imports.exception.NullPropertyImportException;
import com.equinox.imports.exception.TooLongPropertyImportException;
import com.equinox.imports.file.ImportFile;

public class ImportProperty extends AbstractImportField {

	private ImportFile file;
	private String name;
	private Integer length;
	private Boolean notNull;
	private String defaultValue;
	private Boolean multiple;

	public ImportProperty() {
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

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(Boolean notNull) {
		this.notNull = notNull;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}

	@Override
	public <T> T getValue(Class<T> type, ImportLineColumn column) throws ImportPropertyException {

		String stringValue = this.defaultValue;

		if (column != null) {
			stringValue = column.getValue();
		}

		T value = null;

		try {
			value = getValue(type, stringValue);
		} catch (NumberFormatException | ClassCastException e) {
			throw new InvalidFormatPropertyImportException(this, stringValue);
		}

		if (value == null && this.notNull) {
			throw new NullPropertyImportException(this);
		}

		if (value != null && this.length != null && value.toString().length() > this.length) {
			throw new TooLongPropertyImportException(this, value.toString().length());
		}

		return value;
	}
}
