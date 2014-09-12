package com.equinox.imports;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.equinox.imports.exception.ImportException;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.property.ImportProperty;

public class ClassImporter {

	private static final Log logger = LogFactory.getLog(ClassImporter.class);

	private final Class<?> clazz;
	private ImportFile rootFile;
	private final List<ImportProperty> properties;

	public ClassImporter(Class<?> clazz) {
		this.properties = new ArrayList<ImportProperty>();
		this.clazz = clazz;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public void setRootFile(ImportFile file) {
		this.rootFile = file;
	}

	public void addProperty(ImportProperty property) {
		this.properties.add(property);
	}

	public <T> List<T> importFrom(Class<T> clazz, String... files) throws ImportException {

		try {
			if (files.length != rootFile.getFilesCount()) {
				throw new IllegalArgumentException("Il n'y a pas assez de fichiers");
			}

			if (!this.clazz.equals(clazz)) {
				throw new IllegalArgumentException("Classes incompatibles : " + this.clazz.getName() + ", "
						+ clazz.getName());
			}

			// On construit les lignes
			List<ImportLine> lines = rootFile.buildLignes(files);
			List<T> toReturn = new ArrayList<T>();

			for (ImportLine line : lines) {

				try {

					T current = clazz.newInstance();
					for (ImportProperty property : properties) {

						Object value = null;

						if (property.isMultiple()) {
							value = line.getMultipleByProperty(property.getType(), property);

						} else {
							value = line.getByProperty(property.getType(), property);
						}

						BeanUtils.setProperty(current, property.getName(), value);
					}

					toReturn.add(current);

				} catch (ImportException e) {
					logger.error(e.getMessage());
				}
			}

			return toReturn;
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException | IOException
				| ImportException e) {
			logger.error(e, e);
			throw new ImportException("Import impossible", e);
		}

	}

}
