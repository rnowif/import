package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportException;

public interface ImportPropertyTransformer {

	Object transformProperty(String value) throws ImportException;

}
