package com.equinox.imports.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.xerces.impl.io.UCSReader;

import com.equinox.imports.ImportLine;
import com.equinox.imports.exception.ImportException;
import com.equinox.imports.exception.ParseFileImportException;

public class CSVImportFile extends AbstractImportFile {

	private final Integer skiplines;
	private final String startTag;
	private final String endTag;
	private final String delimitor;
	private final Integer fileNumber;
	private final Boolean labels;
	private final Boolean littleEndian;

	public CSVImportFile(String id, Integer fileNumber, Integer skiplines, String startTag, String endTag,
			String delimitor, Boolean labels, Boolean littleEndian) {
		super(id);
		this.skiplines = skiplines;
		this.startTag = startTag;
		this.endTag = endTag;
		this.delimitor = delimitor;
		this.fileNumber = fileNumber;
		this.labels = labels;
		this.littleEndian = littleEndian;
	}

	@Override
	protected List<ImportLine> buildLignesSpecifique(String fileName) throws ImportException {

		List<ImportLine> toReturn = new ArrayList<ImportLine>();

		long lineNumber = 0;

		try {
			FileInputStream f = new FileInputStream(fileName);
			BufferedReader bufferedReader = null;
			try {
				// le fichier est encode en UCS-2 Little Endian
				if (this.littleEndian) {
					bufferedReader = new BufferedReader(new UCSReader(f, UCSReader.UCS2LE));
				} else {
					bufferedReader = new BufferedReader(new InputStreamReader(f));
				}

				lineNumber = skipLines(bufferedReader, lineNumber);

				// on lit les lignes suivantes jusqu'à baliseFin
				boolean labelsRead = labels ? false : true;
				Map<Integer, String> labelsMap = null;
				String line;

				while ((line = bufferedReader.readLine()) != null
						&& (StringUtils.isBlank(endTag) || !StringUtils.equals(line, endTag))) {
					lineNumber++;

					if (!labelsRead) {
						// On lit les intitulés
						String[] labelsString = new StrTokenizer(line, this.delimitor).setIgnoreEmptyTokens(false)
								.setTrimmerMatcher(StrMatcher.trimMatcher()).getTokenArray();

						labelsMap = new HashMap<Integer, String>();

						for (int i = 0; i < labelsString.length; i++) {
							labelsMap.put(i, labelsString[i]);
						}
					} else {
						if (StringUtils.isNotBlank(line)) {
							toReturn.add(processLine(lineNumber, line, labelsMap));
						}
					}
				}

				if (!StringUtils.isBlank(endTag) && !StringUtils.equals(line, endTag)) {
					throw new ParseFileImportException("Impossible de trouver la balise de fin : " + endTag, this);
				}

			} finally {
				bufferedReader.close();
				f.close();
			}
		} catch (IOException e) {
			String message = "Erreur d'E/S lors de la lecture du fichier : " + fileName;
			throw new ImportException(message, e);
		}
		return toReturn;
	}

	private ImportLine processLine(long lineNumber, String line, Map<Integer, String> labelsMap) {

		ImportLine toReturn = new ImportLine(this, lineNumber);

		String[] columns = new StrTokenizer(line, this.delimitor).setIgnoreEmptyTokens(false)
				.setTrimmerMatcher(StrMatcher.trimMatcher()).getTokenArray();

		for (int i = 0; i < columns.length; i++) {
			String value = StringUtils.strip(columns[i], "\"");
			if (this.labels) {
				toReturn.addColumn(labelsMap.get(i), value);
			} else {
				toReturn.addColumn(String.valueOf(i), value);
			}
		}

		return toReturn;
	}

	private long skipLines(BufferedReader bufferedReader, long numeroLigne) throws IOException, ImportException {
		// on passe les lignes d'en-tête
		while (numeroLigne < skiplines && bufferedReader.readLine() != null) {
			numeroLigne++;
		}

		// on passe toutes les lignes jusqu'à baliseDebut
		if (StringUtils.isNotBlank(startTag)) {
			String line;
			do {
				line = bufferedReader.readLine();
				numeroLigne++;
			} while (line != null && !StringUtils.equals(line, startTag));

			if (!StringUtils.equals(line, startTag)) {
				throw new ParseFileImportException("Impossible de trouver la balise de début : " + startTag, this);
			}
		}

		return numeroLigne;
	}

	@Override
	protected String extractFile(String[] files) throws FileNotFoundException {
		// On recherche le fichier associé au fichier courant
		String toReturn = null;
		if (files.length == 1 && this.fileNumber == 1) {
			toReturn = files[0];
		} else {
			for (String file : files) {
				String fileName = new File(file).getName();
				if (fileName.startsWith(this.fileNumber + "_")) {
					toReturn = file;
					break;
				}
			}
		}

		if (StringUtils.isEmpty(toReturn)) {
			throw new FileNotFoundException("Impossible de trouver le fichier : " + this.fileNumber);
		}

		return toReturn;
	}
}
