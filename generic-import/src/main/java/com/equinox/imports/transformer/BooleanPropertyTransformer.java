package com.equinox.imports.transformer;

import org.apache.commons.lang.StringUtils;

import com.equinox.imports.exception.ImportPropertyException;

public class BooleanPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Boolean transformProperty(String value) throws ImportPropertyException {

		if (StringUtils.isEmpty(value)) {
			return null;
		}

		return "1".equals(value) || "true".equals(value);
	}

}
