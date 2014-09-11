package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportException;

public class DoublePropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Double transformProperty(String value) throws ImportException {
		return Double.parseDouble(value);
	}
}
