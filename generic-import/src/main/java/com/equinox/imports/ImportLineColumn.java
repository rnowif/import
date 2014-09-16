package com.equinox.imports;

import com.equinox.imports.file.ImportFile;

public class ImportLineColumn {

	private final String value;
	private final String index;
	private final ImportFile file;

	public ImportLineColumn(ImportFile file, String index, String value) {
		this.value = value;
		this.index = index;
		this.file = file;
	}

	public String getIndex() {
		return index;
	}

	public ImportFile getFile() {
		return file;
	}

	public String getValue() {
		return value;
	}

}
