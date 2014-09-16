package com.equinox.imports.file;

public interface ImportFileFilter {

	/**
	 * True if the line must be kept
	 * 
	 * @param value
	 * @return
	 */
	boolean filter(String value);

}
