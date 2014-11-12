package com.equinox.imports;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import com.equinox.imports.exception.ImportLineException;
import com.equinox.imports.exception.ImportPropertyException;
import com.equinox.imports.exception.NullPropertyImportException;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.file.ImportFileJoin;
import com.equinox.imports.property.ComponentImportProperty;
import com.equinox.imports.property.CompositeImportProperty;
import com.equinox.imports.property.ImportKey;
import com.equinox.imports.property.ImportProperty;
import com.equinox.imports.property.SubClassImportProperty;

public class ImportLine {

	private final ImportFile file;
	private final Long number;
	// Clé numéro 1 : file id, Clé numéro 2 : column index
	private final Map<MultiKey, ImportLineColumn> columns;
	private final Map<String, List<ImportLine>> joinLines;
	private ImportLine parent;

	public ImportLine(ImportFile file, Long number) {
		this.file = file;
		this.number = number;
		this.columns = new HashMap<MultiKey, ImportLineColumn>();
		this.joinLines = new HashMap<String, List<ImportLine>>();
	}

	public void addColumn(String columnIndex, String value) {
		MultiKey key = new MultiKey(this.file.getId(), columnIndex);
		this.columns.put(key, new ImportLineColumn(this.file, columnIndex, value));
	}

	public Long getNumber() {
		return number;
	}

	public ImportLine getParent() {
		return parent;
	}

	private void setParent(ImportLine parent) {
		this.parent = parent;
	}

	public String getStringValueByColumn(String columnIndex) {
		ImportLineColumn column = getFirstColumn(this.file, columnIndex);

		if (column == null) {
			return null;
		}

		return column.getValue();
	}

	private Object getByKey(ImportKey key) {

		ImportLineColumn column = getFirstColumn(this.file, key.getColumnIndex());

		if (column == null) {
			return null;
		}

		try {
			return key.getValue(key.getType(), column);
		} catch (ImportPropertyException e) {
			return null;
		}
	}

	private ImportLineColumn getFirstColumn(ImportFile fileToSearch, String index) {
		List<ImportLineColumn> columns = getColumns(fileToSearch, index);

		if (CollectionUtils.isEmpty(columns)) {
			return null;
		}

		return columns.get(0);
	}

	private List<ImportLineColumn> getColumns(ImportFile fileToSearch, String index) {

		List<ImportLineColumn> toReturn = new ArrayList<ImportLineColumn>();
		MultiKey key = new MultiKey(fileToSearch.getId(), index);
		ImportLineColumn column = columns.get(key);
		if (column != null) {
			toReturn.add(column);
		}

		for (List<ImportLine> lines : joinLines.values()) {
			for (ImportLine line : lines) {
				toReturn.addAll(line.getColumns(fileToSearch, index));
			}
		}

		return toReturn;
	}

	public boolean isEmpty() {
		return this.columns.isEmpty() && this.joinLines.isEmpty();
	}

	public void join(List<ImportLine> joinLines, List<ImportFileJoin> joins) {

		List<ImportLine> filteredLines = new ArrayList<ImportLine>();

		for (ImportLine joinLine : joinLines) {
			// On ne join la ligne que si toutes les jointures correspondent
			boolean toKeep = true;

			for (ImportFileJoin join : joins) {
				Object keyRefValue = getByKey(join.getKeyRef());
				if (keyRefValue == null || !keyRefValue.equals(joinLine.getByKey(join.getJoinKey()))) {
					toKeep = false;
				}
			}

			if (toKeep) {
				filteredLines.add(joinLine);
			}
		}

		String keyId = "";
		for (ImportFileJoin join : joins) {
			keyId += join.getKeyRef().getId();
		}

		join(keyId, filteredLines);
	}

	public void join(String keyId, List<ImportLine> lines) {
		if (!joinLines.containsKey(keyId)) {
			joinLines.put(keyId, new ArrayList<ImportLine>());
		}

		for (ImportLine line : lines) {
			joinLines.get(keyId).add(line);
			line.setParent(this);
		}
	}

	public <T> T parseClass(Class<T> clazz, List<ImportProperty> properties, List<SubClassImportProperty> subClasses,
			List<CompositeImportProperty> compositeProperties, ImportClassPostProcessor processor)
			throws ImportLineException {

		// On applanit la ligne pour merger toutes les lignes jointes qui sont seules
		flatten();

		try {
			T dto = parseClassWithoutFlattening(clazz, properties, subClasses, compositeProperties);

			if (processor != null) {
				processor.postProcess(dto);
			}

			return dto;

		} catch (ImportPropertyException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new ImportLineException(this, e.getMessage());
		}

	}

	private <T> T parseClassWithoutFlattening(Class<T> clazz, List<ImportProperty> properties,
			List<SubClassImportProperty> subClasses, List<CompositeImportProperty> compositeProperties)
			throws ImportLineException, InstantiationException, IllegalAccessException, InvocationTargetException,
			ImportPropertyException {

		T current = clazz.newInstance();
		for (ImportProperty property : properties) {

			List<ImportTuple> tuples = getTuples(Arrays.asList(property));

			Object value = null;

			if (CollectionUtils.isEmpty(tuples)) {
				value = getByTuple(property.getType(), property, null);
			} else if (property.isMultiple()) {

				List<Object> values = new ArrayList<Object>();
				for (ImportTuple tuple : tuples) {
					values.add(getByTuple(property.getType(), property, tuple));
				}
				value = values;

			} else {
				value = getByTuple(property.getType(), property, tuples.get(0));
			}

			BeanUtils.setProperty(current, property.getName(), value);
		}

		for (CompositeImportProperty property : compositeProperties) {

			List<ImportTuple> tuples = getTuples(property);

			Object value = null;

			if (CollectionUtils.isEmpty(tuples)) {
				value = property.getValue(property.getType(), null);
			} else if (property.isMultiple()) {

				List<Object> values = new ArrayList<Object>();
				for (ImportTuple tuple : tuples) {
					values.add(property.getValue(property.getType(), tuple));
				}
				value = values;

			} else {
				value = property.getValue(property.getType(), tuples.get(0));
			}

			BeanUtils.setProperty(current, property.getName(), value);
		}

		for (SubClassImportProperty subClass : subClasses) {

			// On récupère les tuples pour les properties non multiple de la classe et qui ont une colonne
			List<ImportProperty> singleProperties = new ArrayList<ImportProperty>();
			for (ImportProperty property : subClass.getProperties()) {
				if (!property.isMultiple() && StringUtils.isNotEmpty(property.getColumnIndex())) {
					singleProperties.add(property);
				}
			}

			List<ImportTuple> tuples = getTuples(singleProperties);

			if (CollectionUtils.isEmpty(tuples)) {
				if (subClass.isNotNull()) {
					throw new NullPropertyImportException(subClass.getName());
				}
			} else {

				Object value = null;

				if (subClass.isMultiple()) {
					List<Object> values = new ArrayList<Object>();

					for (ImportTuple tuple : tuples) {
						values.add(tuple.getLeaf().parseClassWithoutFlattening(subClass.getType(),
								subClass.getProperties(), subClass.getSubClassProperties(),
								subClass.getCompositeProperties()));
					}

					value = values;

				} else {
					value = tuples
							.get(0)
							.getLeaf()
							.parseClassWithoutFlattening(subClass.getType(), subClass.getProperties(),
									subClass.getSubClassProperties(), null);
				}

				BeanUtils.setProperty(current, subClass.getName(), value);
			}
		}

		return current;

	}

	private <T> T getByTuple(Class<T> type, ImportProperty property, ImportTuple tuple) throws ImportPropertyException {

		ImportLineColumn column = null;

		if (tuple != null) {
			column = tuple.get(property.getFile().getId(), property.getColumnIndex());
		}

		if (column == null && property.isNotNull()) {
			throw new NullPropertyImportException(property.getName());
		}

		return property.getValue(type, column);
	}

	private void flatten() {
		// Pour chaque lignes jointes, s'il n'y a qu'un seule ligne, on va récupérer toutes ses colonnes et les ajouter
		// aux colonnes courantes.
		Set<String> keys = new HashSet<String>(joinLines.keySet());

		for (String lineKey : keys) {
			List<ImportLine> lines = joinLines.get(lineKey);

			// On va d'abord applanir toutes les lignes jointes
			for (ImportLine line : lines) {
				line.flatten();
			}

			// Ensuite, on va regarder s'il y a une seule ligne pour cette clé. Si c'est le cas, on va récupérer ses
			// colonnes et ses lignes jointes dans la ligne courante.
			if (lines.size() == 1) {
				ImportLine line = lines.get(0);

				// Récupération des colonnes
				for (ImportLineColumn column : line.columns.values()) {
					MultiKey key = new MultiKey(column.getFile().getId(), column.getIndex());
					this.columns.put(key, column);
				}

				// Récupération des lignes
				for (String key : line.joinLines.keySet()) {
					if (!this.joinLines.containsKey(key)) {
						this.joinLines.put(key, new ArrayList<ImportLine>());
					}

					this.joinLines.get(key).addAll(line.joinLines.get(key));
				}

				// Suppression de la ligne jointe de la map
				this.joinLines.remove(lineKey);
			}
		}

	}

	private List<ImportTuple> getTuples(List<ImportProperty> properties) {

		List<MultiKey> remainingProperties = new ArrayList<>();
		List<MultiKey> remainingNotNullProperties = new ArrayList<>();

		for (ImportProperty property : properties) {
			MultiKey key = new MultiKey(property.getFile().getId(), property.getColumnIndex());
			remainingProperties.add(key);

			if (property.isNotNull()) {
				remainingNotNullProperties.add(key);
			}
		}

		List<ImportLine> leafLines = getLeafLines(remainingProperties, remainingNotNullProperties);

		List<ImportTuple> toReturn = new ArrayList<ImportTuple>();

		for (ImportLine line : leafLines) {
			toReturn.add(ImportTuple.buildFromLeaf(line));
		}

		return toReturn;
	}

	private List<ImportTuple> getTuples(CompositeImportProperty composite) {

		List<MultiKey> remainingProperties = new ArrayList<>();

		for (ComponentImportProperty property : composite.getComponents()) {
			MultiKey key = new MultiKey(property.getFile().getId(), property.getColumnIndex());
			remainingProperties.add(key);
		}

		// Pas de propriété not null pour les composite properties
		List<ImportLine> leafLines = getLeafLines(remainingProperties, new ArrayList<MultiKey>());

		List<ImportTuple> toReturn = new ArrayList<ImportTuple>();

		for (ImportLine line : leafLines) {
			toReturn.add(ImportTuple.buildFromLeaf(line));
		}

		return toReturn;
	}

	private List<ImportLine> getLeafLines(List<MultiKey> remainingProperties, List<MultiKey> remainingNotNullProperties) {
		// On va parcourir toutes les colonnes et on va supprimer des properties celles qu'on trouve.
		// S'il en manque, on va appeler de manière récursive les lignes jointes avec les properties restantes.

		// On copie remaningProperties pour éviter de modifier la valeur de la variable dans la méthode appelante.
		List<MultiKey> remainingPropertiesCopy = new ArrayList<>(remainingProperties);
		List<MultiKey> remainingNotNullPropertiesCopy = new ArrayList<>(remainingNotNullProperties);

		List<ImportLine> toReturn = new ArrayList<ImportLine>();

		for (ImportLineColumn column : columns.values()) {
			MultiKey key = new MultiKey(column.getFile().getId(), column.getIndex());
			remainingPropertiesCopy.remove(key);
			remainingNotNullPropertiesCopy.remove(key);
		}

		// S'il n'y a plus de propriétés à cherchers, on ajoute la ligne en tant que feuille.
		// S'il y en a encore, on va chercher dans les lignes jointes
		if (remainingPropertiesCopy.isEmpty()) {
			toReturn.add(this);
		} else if (joinLines.isEmpty() && remainingNotNullPropertiesCopy.isEmpty()) {
			// Si on arrive à une feuille et qu'il ne reste plus de propriété obligatoire, on la retourne.
			toReturn.add(this);
		} else if (!joinLines.isEmpty()) {
			for (List<ImportLine> lines : joinLines.values()) {
				for (ImportLine line : lines) {
					toReturn.addAll(line.getLeafLines(remainingPropertiesCopy, remainingNotNullPropertiesCopy));
				}
			}
		}

		return toReturn;
	}

	public Collection<ImportLineColumn> getColumns() {
		return this.columns.values();
	}
}