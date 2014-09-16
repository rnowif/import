package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportPropertyException;

public interface ImportPropertyTransformer {

	Object transformProperty(String value) throws ImportPropertyException;

}
