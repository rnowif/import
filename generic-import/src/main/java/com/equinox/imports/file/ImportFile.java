package com.equinox.imports.file;

import java.io.IOException;
import java.util.List;

import com.equinox.imports.ImportLine;
import com.equinox.imports.exception.ImportException;
import com.equinox.imports.property.ImportKey;

public interface ImportFile {

	void addJoin(ImportFileJoin joint);

	int getFilesCount();

	String getId();

	List<ImportLine> buildLignes(String... files) throws ImportException, IOException;

	void addKey(ImportKey key);

	ImportKey getKey(String keyId);

	void addFilter(String column, ImportFileFilter filter);

}
