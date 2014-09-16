package com.equinox.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.equinox.imports.file.CSVImportFile;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.file.ImportFileFilter;
import com.equinox.imports.file.ImportFileJoin;
import com.equinox.imports.file.XLSImportFile;
import com.equinox.imports.file.XMLImportFile;
import com.equinox.imports.property.AbstractImportField;
import com.equinox.imports.property.ImportKey;
import com.equinox.imports.property.ImportProperty;
import com.equinox.imports.property.SubClassImportProperty;
import com.equinox.imports.transformer.BooleanPropertyTransformer;
import com.equinox.imports.transformer.DoublePropertyTransformer;
import com.equinox.imports.transformer.ImportPropertyTransformer;
import com.equinox.imports.transformer.IntegerPropertyTransformer;
import com.equinox.imports.transformer.LongPropertyTransformer;
import com.equinox.imports.transformer.NopPropertyTransformer;

public class ImportBuilder {

	public static Importer buildFrom(Element element) {

		try {

			NodeList classNodes = element.getElementsByTagName("class");
			if (classNodes.getLength() == 0) {
				throw new IllegalArgumentException("Element class obligatoire");
			}

			ImporterImpl importer = new ImporterImpl();

			for (int i = 0; i < classNodes.getLength(); i++) {

				// Lecture de la classe
				Element classElement = (Element) classNodes.item(i);

				ClassImporter classImporter = readClassImporter(classElement);
				importer.addClassImporter(classImporter);
			}

			return importer;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Imposssible de construire l'importer", e);
		}
	}

	private static ClassImporter readClassImporter(Element classElement) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException {

		Class<?> clazz = readClass(classElement, "name", null);
		ClassImporter classImporter = new ClassImporter(clazz);

		// Lecture des fichiers
		List<Element> fileNodes = getChildrenByTagName(classElement, "file");
		if (fileNodes.size() == 0) {
			throw new IllegalArgumentException("Element file obligatoire");
		}

		Map<String, ImportFile> files = new HashMap<String, ImportFile>();

		// On ne lit que le premier élément file
		Element fileNode = fileNodes.get(0);

		ImportFile root = readFile(fileNode, files, null);
		classImporter.setRootFile(root);

		// Lecture des propriétés
		List<Element> propertyNodes = getChildrenByTagName(classElement, "property");
		for (Element propertyNode : propertyNodes) {
			ImportProperty property = readProperty(propertyNode, files);
			classImporter.addProperty(property);
		}

		// Lecture des propriétés sous classe
		List<Element> subClassPropertyNodes = getChildrenByTagName(classElement, "sub-class-property");
		for (Element propertyNode : subClassPropertyNodes) {
			SubClassImportProperty property = readSubClassProperty(propertyNode, files);
			classImporter.addSubClassProperty(property);
		}

		return classImporter;
	}

	private static ImportFile readFile(Element fileNode, Map<String, ImportFile> files, ImportFile parent)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		String id = fileNode.getAttribute("id");
		String fileType = fileNode.getAttribute("type");
		Integer fileNumber = readInteger(fileNode, "file-number", 1);
		Integer skipLines = readInteger(fileNode, "skip-lines", 0);
		String startTag = fileNode.getAttribute("start-tag");
		String endTag = fileNode.getAttribute("end-tag");
		String delimitor = fileNode.getAttribute("delimitor");
		String worksheet = fileNode.getAttribute("worksheet");
		String baseElement = fileNode.getAttribute("base-element");
		Boolean labels = readBoolean(fileNode, "labels", false);
		Boolean littleEndian = readBoolean(fileNode, "little-endian", false);

		ImportFile file = null;
		if ("csv".equals(fileType)) {
			file = new CSVImportFile(id, fileNumber, skipLines, startTag, endTag, delimitor, labels, littleEndian);
		} else if ("xls".equals(fileType)) {
			file = new XLSImportFile(id, worksheet, skipLines, labels);
		} else if ("xml".equals(fileType)) {
			file = new XMLImportFile(id, baseElement);
		}

		files.put(id, file);

		// On lit les clés du fichier.
		List<Element> keyNodes = getChildrenByTagName(fileNode, "key");
		for (Element keyNode : keyNodes) {
			ImportKey key = readKey(keyNode);
			file.addKey(key);

			// Si parent n'est pas nul, on cherche key-ref et la clé associée dans le parent.
			if (parent != null) {

				ImportFileJoin joint = readJoin(keyNode, parent, file, key);
				parent.addJoin(joint);
			}
		}

		// On lit les filtres du fichier
		List<Element> filterNodes = getChildrenByTagName(fileNode, "filter");
		for (Element filterNode : filterNodes) {
			String column = filterNode.getAttribute("column");
			ImportFileFilter filter = readClass(filterNode, "class", null).asSubclass(ImportFileFilter.class)
					.newInstance();

			file.addFilter(column, filter);
		}

		// On cherche les fichiers à joindre
		List<Element> joinNodes = getChildrenByTagName(fileNode, "file");
		for (Element joinNode : joinNodes) {
			readFile(joinNode, files, file);
		}

		return file;
	}

	private static ImportKey readKey(Element keyNode) throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String keyId = keyNode.getAttribute("id");

		ImportKey key = new ImportKey();
		key.setId(keyId);

		readField(keyNode, key);

		return key;
	}

	private static ImportFileJoin readJoin(Element keyNode, ImportFile parent, ImportFile file, ImportKey key) {
		String keyRefId = keyNode.getAttribute("key-ref");
		ImportKey keyRef = parent.getKey(keyRefId);

		if (keyRef == null) {
			throw new IllegalArgumentException("Impossible de trouver la clé de jointure : " + keyRefId);
		}

		return new ImportFileJoin(file, key, keyRef);
	}

	private static ImportProperty readProperty(Element propertyNode, Map<String, ImportFile> files)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		ImportProperty property = new ImportProperty();

		String refId = propertyNode.getAttribute("file-ref");

		String name = propertyNode.getAttribute("name");

		Integer length = readInteger(propertyNode, "length", null);
		Boolean notNull = readBoolean(propertyNode, "not-null", false);
		Boolean multiple = readBoolean(propertyNode, "multiple", false);
		String defaultValue = propertyNode.getAttribute("default-value");

		ImportFile joinFile = files.get(refId);
		if (joinFile == null) {
			throw new IllegalArgumentException("Fichier introuvable : " + refId);
		}

		property.setDefaultValue(defaultValue);
		property.setFile(joinFile);
		property.setLength(length);
		property.setName(name);
		property.setNotNull(notNull);
		property.setMultiple(multiple);

		readField(propertyNode, property);

		return property;
	}

	private static void readField(Element node, AbstractImportField field) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		String columnIndex = node.getAttribute("column");

		// Lecture du type (String par défaut)
		Class<?> type = readClass(node, "type", String.class);

		ImportPropertyTransformer transformer = null;
		if (String.class.equals(type)) {
			transformer = new NopPropertyTransformer();
		} else if (Long.class.equals(type)) {
			transformer = new LongPropertyTransformer();
		} else if (Integer.class.equals(type)) {
			transformer = new IntegerPropertyTransformer();
		} else if (Double.class.equals(type)) {
			transformer = new DoublePropertyTransformer();
		} else if (Boolean.class.equals(type)) {
			transformer = new BooleanPropertyTransformer();
		}

		// Lecture de la classe de transformation
		String transformClassString = node.getAttribute("transform-class");
		if (StringUtils.isNotEmpty(transformClassString)) {
			transformer = Class.forName(transformClassString).asSubclass(ImportPropertyTransformer.class).newInstance();
		}

		if (transformer == null) {
			throw new IllegalArgumentException("Aucun transformer défini pour le type " + type.getSimpleName());
		}

		// Lecture de la classe de génération
		ImportPropertyGenerator generator = null;
		String generatorClassString = node.getAttribute("generator-class");
		if (StringUtils.isNotEmpty(generatorClassString)) {
			generator = Class.forName(generatorClassString).asSubclass(ImportPropertyGenerator.class).newInstance();
		}

		field.setColumnIndex(columnIndex);
		field.setGenerator(generator);
		field.setTransformer(transformer);
		field.setType(type);

	}

	private static SubClassImportProperty readSubClassProperty(Element subClassPropertyNode,
			Map<String, ImportFile> files) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		SubClassImportProperty subClass = new SubClassImportProperty();

		String name = subClassPropertyNode.getAttribute("name");

		Boolean notNull = readBoolean(subClassPropertyNode, "not-null", false);
		Boolean multiple = readBoolean(subClassPropertyNode, "multiple", false);

		Class<?> type = readClass(subClassPropertyNode, "type", null);

		if (type == null) {
			throw new IllegalArgumentException("Aucun type défini pour la propriété " + name);
		}

		subClass.setName(name);
		subClass.setNotNull(notNull);
		subClass.setMultiple(multiple);
		subClass.setType(type);

		// Lecture des propriétés
		List<Element> propertyNodes = getChildrenByTagName(subClassPropertyNode, "property");
		for (Element propertyNode : propertyNodes) {
			ImportProperty property = readProperty(propertyNode, files);
			subClass.addProperty(property);
		}

		// Lecture des sous classes
		List<Element> subClassPropertyNodes = getChildrenByTagName(subClassPropertyNode, "sub-class-property");
		for (Element propertyNode : subClassPropertyNodes) {
			SubClassImportProperty property = readSubClassProperty(propertyNode, files);
			subClass.addSubClassProperty(property);
		}

		return subClass;
	}

	private static Class<?> readClass(Element node, String name, Class<?> defaultClass) throws ClassNotFoundException {
		String typeString = node.getAttribute(name);
		Class<?> type = defaultClass;
		if (StringUtils.isNotEmpty(typeString)) {
			type = Class.forName(typeString);
		}

		return type;
	}

	private static Boolean readBoolean(Element node, String name, Boolean defaultValue) {
		String boolAsString = node.getAttribute(name);
		Boolean bool = defaultValue;
		if (StringUtils.isNotEmpty(boolAsString)) {
			bool = DatatypeConverter.parseBoolean(boolAsString);
		}

		return bool;
	}

	private static Integer readInteger(Element node, String name, Integer defaultValue) {

		String intAsString = node.getAttribute(name);
		Integer integer = defaultValue;
		if (StringUtils.isNotEmpty(intAsString)) {
			integer = DatatypeConverter.parseInt(intAsString);
		}

		return integer;
	}

	private static List<Element> getChildrenByTagName(Element parent, String name) {
		List<Element> nodeList = new ArrayList<Element>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && name.equals(child.getNodeName())) {
				nodeList.add((Element) child);
			}
		}

		return nodeList;
	}

}
