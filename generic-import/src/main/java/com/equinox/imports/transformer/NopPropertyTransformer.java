package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportException;

public class NopPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public String transformProperty(String value) throws ImportException {
		return value;
	}

}
