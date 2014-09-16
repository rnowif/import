package com.equinox.imports.file;

import java.util.List;

import com.equinox.imports.ImportLine;
import com.equinox.imports.property.ImportKey;

public class ImportFileJoin {

	private final ImportFile file;
	// Clé dans le fichier joint
	private final ImportKey joinKey;
	// Clé dans le fichier parent
	private final ImportKey keyRef;

	public ImportFileJoin(ImportFile file, ImportKey joinKey, ImportKey keyRef) {
		this.file = file;
		this.joinKey = joinKey;
		this.keyRef = keyRef;
	}

	public ImportFile getFile() {
		return file;
	}

	public void joinLines(ImportLine line, List<ImportLine> joinLines) {
		line.join(joinLines, keyRef, joinKey);
	}

}
