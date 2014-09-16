package com.equinox.imports;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;

public class ImportTuple {

	// Première clé : file id, deuxième clé : column index
	private final Map<MultiKey, ImportLineColumn> columnsMappedByFileByIndex;
	private ImportLine leaf;

	public ImportTuple() {
		this.columnsMappedByFileByIndex = new HashMap<MultiKey, ImportLineColumn>();
	}

	private void addAll(Collection<ImportLineColumn> columns) {
		for (ImportLineColumn column : columns) {
			addColumn(column);
		}
	}

	private void addColumn(ImportLineColumn column) {
		this.columnsMappedByFileByIndex.put(getKey(column.getFile().getId(), column.getIndex()), column);

	}

	public ImportLineColumn get(String fileId, String index) {
		return this.columnsMappedByFileByIndex.get(getKey(fileId, index));
	}

	private static MultiKey getKey(String fileId, String columnIndex) {
		return new MultiKey(fileId, columnIndex);
	}

	public ImportLine getLeaf() {
		return leaf;
	}

	private void setLeaf(ImportLine leaf) {
		this.leaf = leaf;
	}

	public static ImportTuple buildFromLeaf(ImportLine line) {

		ImportTuple tuple = new ImportTuple();
		tuple.setLeaf(line);

		ImportLine currentLine = line;

		while (currentLine != null) {
			tuple.addAll(line.getColumns());
			currentLine = currentLine.getParent();
		}

		return tuple;
	}

}
