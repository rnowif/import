package com.equinox.imports.transformer;

import org.apache.commons.lang.StringUtils;

import com.equinox.imports.exception.ImportPropertyException;

public class DoublePropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Double transformProperty(String value) throws ImportPropertyException {

		if (StringUtils.isEmpty(value)) {
			return null;
		}

		return Double.parseDouble(value);
	}
}
