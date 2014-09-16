package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportPropertyException;

public class NopPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public String transformProperty(String value) throws ImportPropertyException {
		return value;
	}

}
