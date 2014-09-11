package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportException;

public class LongPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Long transformProperty(String value) throws ImportException {
		return Long.valueOf(value);
	}

}
