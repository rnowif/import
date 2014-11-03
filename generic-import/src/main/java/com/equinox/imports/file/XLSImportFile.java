package com.equinox.imports.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.equinox.imports.ImportLine;
import com.equinox.imports.exception.ImportException;

public class XLSImportFile extends AbstractImportFile {

	public static final String DATE_FORMAT = "yyyy-MM-dd - HH:mm:ss";

	private final String worksheet;
	private final Integer skiplines;
	private final Boolean labels;

	private final DataFormatter dataFormatter;
	private final DateFormat dateFormat;
	private final DecimalFormat decimalFormat;

	public XLSImportFile(String id, String worksheet, Integer skiplines, Boolean labels) {
		super(id);
		this.worksheet = worksheet;
		this.skiplines = skiplines;
		this.labels = labels;
		this.dataFormatter = new DataFormatter();
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		this.decimalFormat = new DecimalFormat("#.##########", symbols);
		this.dateFormat = new SimpleDateFormat(DATE_FORMAT);
	}

	@Override
	protected String extractFile(String[] files) throws IOException {
		// On recherche le fichier qui contient la feuille souhaitée
		String toReturn = null;
		for (String file : files) {
			Sheet sheet = getSheet(file);

			if (sheet != null) {
				toReturn = file;
				break;
			}
		}

		if (StringUtils.isEmpty(toReturn)) {
			throw new FileNotFoundException("Impossible de trouver de fichier avec la feuille : " + this.worksheet);
		}

		return toReturn;
	}

	@Override
	protected List<ImportLine> buildLignesSpecifique(String file) throws ImportException {
		List<ImportLine> toReturn = new ArrayList<ImportLine>();

		try {
			Sheet sheet = getSheet(file);

			Iterator<Row> rowIterator = sheet.rowIterator();

			// on passe les lignes d'en-tête
			for (int i = 0; i < this.skiplines; i++) {
				rowIterator.next();
			}

			// On va lire la ligne d'intitulés pour en extraire les colonnes, si
			// besoin.
			Map<Integer, String> labelsMap = null;

			if (this.labels) {
				labelsMap = new HashMap<Integer, String>();
				Row labelRow = rowIterator.next();

				Iterator<Cell> it = labelRow.cellIterator();
				while (it.hasNext()) {
					Cell currentLabel = it.next();
					labelsMap.put(currentLabel.getColumnIndex(), readCellContent(currentLabel));
				}
			}

			// on lit les lignes suivantes
			while (rowIterator.hasNext()) {

				Row currentRow = rowIterator.next();
				ImportLine line = processLine(currentRow, labelsMap);
				if (line != null) {
					toReturn.add(line);
				}
			}

		} catch (IOException e) {
			String message = "Erreur d'E/S lors de la lecture du fichier : " + file;
			throw new ImportException(message, e);
		}

		return toReturn;
	}

	private ImportLine processLine(Row row, Map<Integer, String> labelsMap) {

		// numLigne + 1 car 0-based
		ImportLine toReturn = new ImportLine(this, Long.valueOf(row.getRowNum() + 1));

		// on ne parcourt pas avec currentRow.cellIterator() car il ne
		// prend en compte que les cellules physiques (il saute les
		// cellules vides)
		int maxColIx = row.getLastCellNum();
		boolean emptyLine = true;
		// on doit parcourir jusqu'au nb de colonnes attendu : sinon, si
		// la dernière cellule de la ligne est vide, on aura une erreur
		// de formatage alors que ce peut être volontaire
		for (int i = 0; i < maxColIx; i++) {
			Cell currentCell = row.getCell(i);
			String content = "";
			if (currentCell != null) {
				content = readCellContent(currentCell);
			}

			if (StringUtils.isNotBlank(content)) {
				emptyLine = false;
			}

			if (this.labels) {
				toReturn.addColumn(labelsMap.get(i), content);
			} else {
				toReturn.addColumn(String.valueOf(i), content);
			}
		}

		if (emptyLine) {
			return null;
		}

		return toReturn;
	}

	private Sheet getSheet(String file) throws IOException {
		Workbook wb = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(file)));
		return wb.getSheet(this.worksheet);
	}

	private String readCellContent(Cell cell) {
		// on utilise un dataFormatter qui va convertir les cellules en String
		// (sinon on a une erreur en faisant un getCelleStringValue sur une
		// cellule numérique), ainsi qu'un formulaEvaluator pour calculer le
		// résultat des formules (sinon on va lire la chaine "=A1+B2" au lieu du
		// résultat de ce calcul)
		String toReturn = "";
		if (cell != null) {
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_BLANK:
				toReturn = "";
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				toReturn = "" + cell.getBooleanCellValue();
				break;
			case Cell.CELL_TYPE_STRING:
				toReturn = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					toReturn = dateFormat.format(cell.getDateCellValue());
				} else {
					toReturn = decimalFormat.format(cell.getNumericCellValue());
				}
				break;
			case Cell.CELL_TYPE_FORMULA:
				// s'il s'agit d'une formule, il faut créer un evaluator pour
				// calculer le résultat (sinon c'est la formule elle-même qui
				// est retournée au lieu du résultat)
				FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
				// Il faut à nouveau faire un switch sur le type de la cellule.
				// En effet, il peut y avoir une cellule de type texte qui a la
				// forme d'une cellule numérique. Dans ce cas, il faut retourner
				// une String et nom un nombre.
				switch (cell.getCachedFormulaResultType()) {
				case Cell.CELL_TYPE_ERROR:
					toReturn = "#VALUE!";
					break;
				case Cell.CELL_TYPE_NUMERIC:
					if (HSSFDateUtil.isCellDateFormatted(cell)) {
						toReturn = dateFormat.format(cell.getDateCellValue());
					} else {
						toReturn = decimalFormat.format(cell.getNumericCellValue());
					}
					break;
				default:
					toReturn = dataFormatter.formatCellValue(cell, evaluator);
					break;
				}
				break;
			}
		}
		return toReturn;
	}

}
