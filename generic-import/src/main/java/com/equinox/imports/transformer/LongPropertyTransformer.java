package com.equinox.imports.transformer;

import org.apache.commons.lang.StringUtils;

import com.equinox.imports.exception.ImportPropertyException;

public class LongPropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Long transformProperty(String value) throws ImportPropertyException {

		if (StringUtils.isEmpty(value)) {
			return null;
		}

		return Long.valueOf(value);
	}

}
