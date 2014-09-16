package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportPropertyException;

public class BooleanPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Boolean transformProperty(String value) throws ImportPropertyException {
		return "1".equals(value) || "true".equals(value);
	}

}
