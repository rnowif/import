package com.equinox.imports.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.equinox.imports.ImportLine;
import com.equinox.imports.exception.ImportException;
import com.equinox.imports.exception.ParseFileImportException;
import com.equinox.imports.property.ImportKey;

public abstract class AbstractImportFile implements ImportFile {

	private static final Log logger = LogFactory.getLog(AbstractImportFile.class);

	private final Map<ImportFile, List<ImportFileJoin>> joints;
	private final Map<String, ImportKey> keys;
	private final Map<String, ImportFileFilter> filters;
	private final String id;

	public AbstractImportFile(String id) {
		this.joints = new HashMap<ImportFile, List<ImportFileJoin>>();
		this.keys = new HashMap<String, ImportKey>();
		this.filters = new HashMap<String, ImportFileFilter>();
		this.id = id;
	}

	@Override
	public void addJoin(ImportFileJoin joint) {
		if (!this.joints.containsKey(joint.getFile())) {
			this.joints.put(joint.getFile(), new ArrayList<ImportFileJoin>());
		}

		joints.get(joint.getFile()).add(joint);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void addKey(ImportKey key) {
		this.keys.put(key.getId(), key);
	}

	@Override
	public ImportKey getKey(String keyId) {
		return this.keys.get(keyId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractImportFile other = (AbstractImportFile) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public List<ImportLine> buildLignes(String... files) throws ImportException, IOException {

		String file = extractFile(files);

		logger.info("Ouverture du fichier " + file);

		List<ImportLine> linesUnfiltered = buildLignesSpecifique(file);

		List<ImportLine> lines = new ArrayList<ImportLine>();

		if (filters.isEmpty()) {
			lines = linesUnfiltered;
		} else {
			for (ImportLine line : linesUnfiltered) {

				boolean keep = true;
				for (Entry<String, ImportFileFilter> entry : this.filters.entrySet()) {
					String value = line.getStringValueByColumn(entry.getKey());
					ImportFileFilter filter = entry.getValue();

					if (!filter.filter(value)) {
						keep = false;
						break;
					}
				}

				if (keep) {
					lines.add(line);
				} else {
					logger.debug("Ligne " + line.getNumber() + " ignorée par l'application du filtre");
				}
			}
		}

		logger.info("Lignes lues dans le fichier : " + lines.size());

		for (ImportFile joinFile : this.joints.keySet()) {
			List<ImportLine> joinLines = joinFile.buildLignes(files);

			for (ImportLine line : lines) {
				line.join(joinLines, this.joints.get(joinFile));
			}
		}

		return lines;
	}

	@Override
	public void addFilter(String column, ImportFileFilter filter) {
		this.filters.put(column, filter);
	}

	protected abstract String extractFile(String[] files) throws IOException, ParseFileImportException;

	protected abstract List<ImportLine> buildLignesSpecifique(String file) throws ImportException;

}
