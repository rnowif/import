package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportPropertyException;

public class IntegerPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Integer transformProperty(String value) throws ImportPropertyException {
		return Integer.valueOf(value);
	}

}
