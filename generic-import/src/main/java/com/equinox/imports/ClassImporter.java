package com.equinox.imports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.equinox.imports.exception.ImportException;
import com.equinox.imports.exception.ImportLineException;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.property.CompositeImportProperty;
import com.equinox.imports.property.ImportProperty;
import com.equinox.imports.property.SubClassImportProperty;

public class ClassImporter {

	private static final Log logger = LogFactory.getLog(ClassImporter.class);

	private final Class<?> clazz;
	private ImportFile rootFile;
	private ImportClassPostProcessor processor;
	private final List<ImportProperty> properties;
	private final List<SubClassImportProperty> subClassProperties;
	private final List<CompositeImportProperty> compositeProperties;

	public ClassImporter(Class<?> clazz) {
		this.properties = new ArrayList<ImportProperty>();
		this.subClassProperties = new ArrayList<SubClassImportProperty>();
		this.compositeProperties = new ArrayList<CompositeImportProperty>();
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

	public void addSubClassProperty(SubClassImportProperty property) {
		this.subClassProperties.add(property);
	}

	public void addCompositeProperty(CompositeImportProperty property) {
		this.compositeProperties.add(property);
	}

	public <T> List<T> importFrom(Class<T> clazz, String... files) throws ImportException {

		try {

			if (!this.clazz.equals(clazz)) {
				throw new IllegalArgumentException("Classes incompatibles : " + this.clazz.getName() + ", "
						+ clazz.getName());
			}

			// On construit les lignes
			List<ImportLine> lines = rootFile.buildLignes(files);
			List<T> toReturn = new ArrayList<T>();

			for (ImportLine line : lines) {

				try {

					T dto = line.parseClass(clazz, this.properties, this.subClassProperties, this.compositeProperties,
							this.processor);

					toReturn.add(dto);

				} catch (ImportLineException e) {
					logger.error(e.getMessage());
				}
			}

			return toReturn;
		} catch (IOException e) {
			logger.error(e, e);
			throw new ImportException("Import impossible", e);
		}

	}

	public void setProcessor(ImportClassPostProcessor processor) {
		this.processor = processor;
	}
}
