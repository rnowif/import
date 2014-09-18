package com.equinox.imports.transformer;

import org.apache.commons.lang.StringUtils;

import com.equinox.imports.exception.ImportPropertyException;

public class IntegerPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Integer transformProperty(String value) throws ImportPropertyException {

		if (StringUtils.isEmpty(value)) {
			return null;
		}

		return Integer.valueOf(value);
	}

}
