package com.equinox.imports;

import com.equinox.imports.exception.ImportPropertyException;

public interface ImportClassPostProcessor {

	public void postProcess(Object object) throws ImportPropertyException;

}
