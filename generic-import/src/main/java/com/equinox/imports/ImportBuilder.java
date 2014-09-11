package com.equinox.imports;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.equinox.imports.file.CSVImportFile;
import com.equinox.imports.file.ImportFile;
import com.equinox.imports.file.ImportFileJoin;
import com.equinox.imports.file.XLSImportFile;
import com.equinox.imports.file.XMLImportFile;
import com.equinox.imports.property.ImportKey;
import com.equinox.imports.property.ImportProperty;
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
			if (classNodes == null) {
				throw new IllegalArgumentException("Element class obligatoire");
			}

			ImporterImpl importer = new ImporterImpl();

			for (int i = 0; i < classNodes.getLength(); i++) {

				// Lecture de la classe
				Element classElement = (Element) classNodes.item(i);

				Class<?> clazz = readClass(classElement, "name", null);
				ClassImporter classImporter = new ClassImporter(clazz);

				// Lecture des fichiers
				NodeList fileNodes = classElement.getElementsByTagName("file");
				if (fileNodes == null) {
					throw new IllegalArgumentException("Element file obligatoire");
				}

				Map<String, ImportFile> files = new HashMap<String, ImportFile>();

				// On ne lit que le premier élément file
				Element fileNode = (Element) fileNodes.item(0);

				ImportFile root = readFile(fileNode, files, null);

				classImporter.setRootFile(root);

				// Lecture des propriétés
				NodeList propertyNodes = classElement.getElementsByTagName("property");
				if (propertyNodes != null) {
					for (int j = 0; j < propertyNodes.getLength(); j++) {
						Element propertyNode = (Element) propertyNodes.item(j);

						String refId = propertyNode.getAttribute("file-ref");

						String name = propertyNode.getAttribute("name");
						String columnIndex = propertyNode.getAttribute("column");

						// Lecture du type (String par défaut)
						Class<?> type = readClass(propertyNode, "type", String.class);

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
						String transformClassString = propertyNode.getAttribute("transform-class");
						if (StringUtils.isNotEmpty(transformClassString)) {
							transformer = Class.forName(transformClassString)
									.asSubclass(ImportPropertyTransformer.class).newInstance();
						}

						if (transformer == null) {
							throw new IllegalArgumentException("Aucun transformer défini pour le type "
									+ type.getSimpleName());
						}

						Integer length = readInteger(propertyNode, "length", null);
						Boolean notNull = readBoolean(propertyNode, "not-null", false);
						Boolean multiple = readBoolean(propertyNode, "multiple", false);
						String defaultValue = propertyNode.getAttribute("default-value");

						ImportFile joinFile = files.get(refId);
						if (joinFile == null) {
							throw new IllegalArgumentException("Fichier introuvable : " + refId);
						}

						ImportProperty property = new ImportProperty(joinFile, name, columnIndex, type, length,
								notNull, defaultValue, transformer, multiple);

						classImporter.addProperty(property);
					}
				}

				importer.addClassImporter(classImporter);
			}

			return importer;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException("Imposssible de construire l'importer", e);
		}
	}

	private static Class<?> readClass(Element node, String name, Class<?> defaultClass) throws ClassNotFoundException {
		String typeString = node.getAttribute(name);
		Class<?> type = defaultClass;
		if (StringUtils.isNotEmpty(typeString)) {
			type = Class.forName(typeString);
		}

		return type;
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
		NodeList keyNodes = fileNode.getElementsByTagName("key");
		if (keyNodes == null) {
			throw new IllegalArgumentException("Une clé doit forcément être définie");
		}

		if (keyNodes != null) {
			for (int i = 0; i < keyNodes.getLength(); i++) {
				Element keyNode = (Element) keyNodes.item(i);

				String keyId = keyNode.getAttribute("id");
				String keyColumnIndex = keyNode.getAttribute("column");

				// Lecture du type (String par défaut)
				Class<?> keyType = readClass(keyNode, "type", String.class);

				ImportPropertyTransformer transformer = null;
				if (String.class.equals(keyType)) {
					transformer = new NopPropertyTransformer();
				} else if (Long.class.equals(keyType)) {
					transformer = new LongPropertyTransformer();
				} else if (Integer.class.equals(keyType)) {
					transformer = new IntegerPropertyTransformer();
				} else if (Double.class.equals(keyType)) {
					transformer = new DoublePropertyTransformer();
				} else if (Boolean.class.equals(keyType)) {
					transformer = new BooleanPropertyTransformer();
				}

				// Lecture de la classe de transformation
				String transformClassString = keyNode.getAttribute("transform-class");
				if (StringUtils.isNotEmpty(transformClassString)) {
					transformer = Class.forName(transformClassString).asSubclass(ImportPropertyTransformer.class)
							.newInstance();
				}

				if (transformer == null) {
					throw new IllegalArgumentException("Aucun transformer défini pour le type "
							+ keyType.getSimpleName());
				}

				ImportKey key = new ImportKey(keyId, keyType, keyColumnIndex, transformer);

				file.addKey(key);

				// Si parent n'est pas nul, on cherche key-ref et la clé associée dans le parent.
				if (parent != null) {
					String keyRefId = keyNode.getAttribute("key-ref");
					ImportKey keyRef = parent.getKey(keyRefId);

					if (keyRef == null) {
						throw new IllegalArgumentException("Impossible de trouver la clé de jointure : " + keyRefId);
					}

					ImportFileJoin joint = new ImportFileJoin(file, key, keyRef);

					parent.addJoin(joint);
				}
			}
		}

		// On cherche les fichiers à joindre
		NodeList joinNodes = fileNode.getElementsByTagName("file");
		if (joinNodes != null) {
			for (int i = 0; i < joinNodes.getLength(); i++) {
				Element joinNode = (Element) joinNodes.item(i);
				readFile(joinNode, files, file);
			}
		}

		return file;
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

}
