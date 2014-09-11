package com.equinox.imports.property;

import com.equinox.imports.transformer.ImportPropertyTransformer;

public class ImportKey {

	private final String id;
	private final Class<?> type;
	private final String columnIndex;
	private final ImportPropertyTransformer transformer;

	public ImportKey(String id, Class<?> type, String columnName, ImportPropertyTransformer transformer) {
		this.id = id;
		this.type = type;
		this.columnIndex = columnName;
		this.transformer = transformer;
	}

	public String getId() {
		return id;
	}

	public Class<?> getType() {
		return type;
	}

	public String getColumnIndex() {
		return columnIndex;
	}

	public ImportPropertyTransformer getTransformer() {
		return transformer;
	}

}
