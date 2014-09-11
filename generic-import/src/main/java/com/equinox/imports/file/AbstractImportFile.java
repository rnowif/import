package com.equinox.imports.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final String id;

	public AbstractImportFile(String id) {
		this.joints = new ArrayList<ImportFileJoin>();
		this.keys = new HashMap<String, ImportKey>();
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

		List<ImportLine> lines = buildLignesSpecifique(file);

		logger.info("Lignes lues dans le fichier : " + lines.size());

		for (ImportFileJoin join : getJoints()) {
			List<ImportLine> joinLines = join.getFile().buildLignes(files);

			for (ImportLine line : lines) {
				join.joinLines(line, joinLines);
			}
		}

		return lines;
	}

	protected abstract String extractFile(String[] files) throws IOException, ParseFileImportException;

	protected abstract List<ImportLine> buildLignesSpecifique(String file) throws ImportException;

}
