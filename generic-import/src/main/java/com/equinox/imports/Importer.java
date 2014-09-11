package com.equinox.imports;

import java.util.List;

import com.equinox.imports.exception.ImportException;

public interface Importer {

	public <T> List<T> importFrom(Class<T> clazz, String... files) throws ImportException;

}
