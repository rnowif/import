package com.equinox.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.equinox.imports.exception.ImportException;
import com.equinox.imports.exception.ImportLineException;
import com.equinox.imports.exception.InvalidFormatPropertyImportException;
import com.equinox.imports.exception.NullPropertyImportException;
import com.equinox.imports.exception.TooLongPropertyImportException;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.property.ImportKey;
import com.equinox.imports.property.ImportProperty;
import com.equinox.imports.transformer.ImportPropertyTransformer;

public class ImportLine {

	private final ImportFile file;
	private final Long number;
	private final Map<String, ImportLineColumn> columns;
	private final List<ImportLine> joinLines;

	public ImportLine(ImportFile file, Long number) {
		this.file = file;
		this.number = number;
		this.columns = new HashMap<String, ImportLineColumn>();
		this.joinLines = new ArrayList<ImportLine>();
	}

	public <T> T getByKey(Class<T> clazz, ImportKey key) {

		ImportLineColumn column = columns.get(key.getColumnIndex());

		try {
			return getValue(clazz, column.getValue(), key.getTransformer(), null);
		} catch (NumberFormatException e) {
			return null;
		} catch (ImportException e) {
			return null;
		}
	}

	public <T> T getByProperty(Class<T> type, ImportProperty property) throws ImportException {

		ImportLineColumn column = getColumn(property.getFile(), property.getColumnIndex());

		if (column == null && property.isNotNull()) {
			throw new ImportLineException(this, "La colonne " + property.getColumnIndex() + " n'existe pas");
		}

		return getByColumn(column, type, property);
	}

	public <T> List<T> getMultipleByProperty(Class<T> type, ImportProperty property) throws ImportException {

		List<T> toReturn = new ArrayList<T>();

		List<ImportLineColumn> columns = getColumns(property.getFile(), property.getColumnIndex());

		if (CollectionUtils.isEmpty(columns) && property.isNotNull()) {
			throw new ImportLineException(this, "La colonne " + property.getColumnIndex() + " n'existe pas");
		}

		for (ImportLineColumn column : columns) {
			toReturn.add(getByColumn(column, type, property));
		}

		return toReturn;
	}

	private <T> T getByColumn(ImportLineColumn column, Class<T> type, ImportProperty property) throws ImportException {

		String stringValue = property.getDefaultValue();

		if (column != null) {
			stringValue = column.getValue();
		}

		T value = null;

		try {
			value = getValue(type, stringValue, property.getTransformer(), property.getGenerator());
		} catch (NumberFormatException | ClassCastException e) {
			throw new InvalidFormatPropertyImportException(this, property, stringValue);
		} catch (ImportException e) {
			throw new ImportLineException(this, e.getMessage());
		}

		if (value == null && property.isNotNull()) {
			throw new NullPropertyImportException(this, property);
		}

		if (value != null && property.getLength() != null && value.toString().length() > property.getLength()) {
			throw new TooLongPropertyImportException(this, property, value.toString().length());
		}

		return value;
	}

	private <T> T getValue(Class<T> type, String stringValue, ImportPropertyTransformer transformer,
			ImportPropertyGenerator generator) throws ImportException {

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

	private ImportLineColumn getColumn(ImportFile fileToSearch, String index) {

		if (this.file.getId().equals(fileToSearch.getId())) {
			return columns.get(index);
		}

		for (ImportLine joinLine : joinLines) {
			return joinLine.getColumn(fileToSearch, index);
		}

		return null;
	}

	private List<ImportLineColumn> getColumns(ImportFile fileToSearch, String index) {

		List<ImportLineColumn> toReturn = new ArrayList<ImportLineColumn>();

		if (this.file.getId().equals(fileToSearch.getId())) {
			ImportLineColumn column = columns.get(index);
			if (column != null) {
				toReturn.add(column);
			}
		}

		for (ImportLine joinLine : joinLines) {
			toReturn.addAll(joinLine.getColumns(fileToSearch, index));
		}

		return toReturn;
	}

	public void addJoinLine(ImportLine joinLine) {
		joinLines.add(joinLine);
	}

	public void addColumn(String columnIndex, String value) {
		this.columns.put(columnIndex, new ImportLineColumn(value));
	}

	public Long getNumber() {
		return number;
	}

}
