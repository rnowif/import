package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportException;

public class BooleanPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Boolean transformProperty(String value) throws ImportException {
		return "1".equals(value) || "true".equals(value);
	}

}
