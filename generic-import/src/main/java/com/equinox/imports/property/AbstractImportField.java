package com.equinox.imports.property;

import org.apache.commons.lang.StringUtils;

import com.equinox.imports.ImportLineColumn;
import com.equinox.imports.ImportPropertyGenerator;
import com.equinox.imports.exception.ImportPropertyException;
import com.equinox.imports.transformer.ImportPropertyTransformer;

public abstract class AbstractImportField {

	private Class<?> type;
	private String columnIndex;
	private ImportPropertyTransformer transformer;
	private ImportPropertyGenerator generator;

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(String columnIndex) {
		this.columnIndex = columnIndex;
	}

	public ImportPropertyTransformer getTransformer() {
		return transformer;
	}

	public void setTransformer(ImportPropertyTransformer transformer) {
		this.transformer = transformer;
	}

	public ImportPropertyGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(ImportPropertyGenerator generator) {
		this.generator = generator;
	}

	public abstract <T> T getValue(Class<T> type, ImportLineColumn column) throws ImportPropertyException;

	protected <T> T getValue(Class<T> type, String stringValue) throws ImportPropertyException {

		if (StringUtils.isEmpty(stringValue) && generator == null) {
			return null;
		}

		T value = null;

		if (generator != null) {
			value = type.cast(generator.generate());
		} else {
			value = type.cast(transformer.transformProperty(stringValue));
		}

		if (value != null && StringUtils.isEmpty(value.toString())) {
			return null;
		}

		return value;
	}

}
