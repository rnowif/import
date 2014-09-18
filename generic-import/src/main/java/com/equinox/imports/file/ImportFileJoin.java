package com.equinox.imports.file;

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

	public ImportKey getJoinKey() {
		return joinKey;
	}

	public ImportKey getKeyRef() {
		return keyRef;
	}
}
