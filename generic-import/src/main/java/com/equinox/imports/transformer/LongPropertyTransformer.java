package com.equinox.imports.transformer;

import com.equinox.imports.exception.ImportPropertyException;

public class LongPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Long transformProperty(String value) throws ImportPropertyException {
		return Long.valueOf(value);
	}

}
