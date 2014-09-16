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

	private final List<ImportFileJoin> joints;
	private final Map<String, ImportKey> keys;
	private final Map<String, ImportFileFilter> filters;
	private final String id;

	public AbstractImportFile(String id) {
		this.joints = new ArrayList<ImportFileJoin>();
		this.keys = new HashMap<String, ImportKey>();
		this.filters = new HashMap<String, ImportFileFilter>();
		this.id = id;
	}

	@Override
	public void addJoin(ImportFileJoin joint) {
		joints.add(joint);
	}

	@Override
	public int getFilesCount() {
		int count = 1;

		for (ImportFileJoin join : joints) {
			count += join.getFile().getFilesCount();
		}

		return count;
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

	protected List<ImportFileJoin> getJoints() {
		return joints;
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

		for (ImportFileJoin join : getJoints()) {
			List<ImportLine> joinLines = join.getFile().buildLignes(files);

			for (ImportLine line : lines) {
				join.joinLines(line, joinLines);
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
