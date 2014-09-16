package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportPropertyException;

public class DoublePropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Double transformProperty(String value) throws ImportPropertyException {
		return Double.parseDouble(value);
	}
}
