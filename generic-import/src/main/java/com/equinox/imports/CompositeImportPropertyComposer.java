package com.equinox.imports;

import java.util.Map;

import com.equinox.imports.exception.ImportPropertyException;

public interface CompositeImportPropertyComposer {

	Object compose(Map<String, String> values) throws ImportPropertyException;

}
