package com.equinox.imports.property;

import com.equinox.imports.ImportPropertyGenerator;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.transformer.ImportPropertyTransformer;

public class ImportProperty {

	private final ImportFile file;
	private final Class<?> type;
	private final String name;
	private final String columnIndex;
	private final Integer length;
	private final Boolean notNull;
	private final ImportPropertyTransformer transformer;
	private final ImportPropertyGenerator generator;
	private final String defaultValue;
	private final Boolean multiple;

	public ImportProperty(ImportFile file, String name, String columnIndex, Class<?> type, Integer length,
			Boolean notNull, String defaultValue, Boolean multiple, ImportPropertyTransformer transformer,
			ImportPropertyGenerator generator) {
		this.file = file;
		this.type = type;
		this.name = name;
		this.columnIndex = columnIndex;
		this.length = length;
		this.notNull = notNull;
		this.transformer = transformer;
		this.generator = generator;
		this.defaultValue = defaultValue;
		this.multiple = multiple;
	}

	public String getColumnIndex() {
		return columnIndex;
	}

	public ImportFile getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public Integer getLength() {
		return length;
	}

	public ImportPropertyTransformer getTransformer() {
		return transformer;
	}

	public ImportPropertyGenerator getGenerator() {
		return generator;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public Boolean isMultiple() {
		return multiple;
	}

	public Class<?> getType() {
		return type;
	}

}
