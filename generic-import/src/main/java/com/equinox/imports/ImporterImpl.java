package com.equinox.imports;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.equinox.imports.exception.ImportException;

public class ImporterImpl implements Importer {

	private final Map<Class<?>, ClassImporter> importers;

	public ImporterImpl() {
		this.importers = new HashMap<Class<?>, ClassImporter>();
	}

	@Override
	public <T> List<T> importFrom(Class<T> clazz, String... files) throws ImportException {

		// On récupère l'importer de la classe
		ClassImporter importer = importers.get(clazz);

		if (importer == null) {
			throw new IllegalArgumentException("Il n'existe pas de mapping défini pour cette classe : "
					+ clazz.getCanonicalName());
		}

		return importer.importFrom(clazz, files);
	}

	public void addClassImporter(ClassImporter classImporter) {
		importers.put(classImporter.getClazz(), classImporter);
	}

}
