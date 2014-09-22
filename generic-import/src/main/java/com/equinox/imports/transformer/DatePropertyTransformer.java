package com.equinox.imports.transformer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.equinox.imports.exception.ImportPropertyException;
import com.equinox.imports.exception.InvalidFormatPropertyImportException;
import com.equinox.imports.file.XLSImportFile;

public class DatePropertyTransformer implements ImportPropertyTransformer {

	@Override
	public Date transformProperty(String value) throws ImportPropertyException {
		try {
			return new SimpleDateFormat(XLSImportFile.DATE_FORMAT).parse(value);
		} catch (ParseException e) {
			throw new InvalidFormatPropertyImportException("date version", XLSImportFile.DATE_FORMAT, value);
		}
	}

}
