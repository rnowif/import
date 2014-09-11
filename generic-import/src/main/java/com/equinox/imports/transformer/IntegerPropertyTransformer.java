package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportException;

public class IntegerPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Integer transformProperty(String value) throws ImportException {
		return Integer.valueOf(value);
	}

}
