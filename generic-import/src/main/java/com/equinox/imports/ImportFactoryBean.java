package com.equinox.imports;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.equinox.imports.utils.ImportUtils;

public class ImportFactoryBean {

	private String[] mappingResources;
	private final Map<String, Element> elementsMappedById;

	public ImportFactoryBean() {
		elementsMappedById = new HashMap<String, Element>();
	}

	public void buildImportFactory() {

		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();

		if (this.mappingResources != null) {
			// Lecture de tous les fichiers de mapping
			for (String mapping : this.mappingResources) {
				Resource resource = new ClassPathResource(mapping.trim(), classLoader);

				Document document = getDocumentXMLFromResource(resource);

				// Lecture de l'id dans le document
				NodeList mappingNodes = document.getElementsByTagName("import-mapping");
				if (mappingNodes != null) {
					for (int i = 0; i < mappingNodes.getLength(); i++) {
						Element header = (Element) mappingNodes.item(0);
						String id = header.getAttribute("id");

						if (StringUtils.isEmpty(id)) {
							throw new IllegalArgumentException("L'attribut id est obligatoire");
						}

						elementsMappedById.put(id, header);
					}
				}
			}
		}
	}

	private Document getDocumentXMLFromResource(Resource resource) {
		// Lecture du fichier en String
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(resource.getInputStream(), writer);
		} catch (IOException e) {
			throw new IllegalArgumentException("Erreur lors du décodage du fichier de mapping", e);
		}
		String contenuXML = writer.toString();

		// Transformation en document XML
		Document document;
		try {
			document = ImportUtils.createDocument(contenuXML);
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new IllegalArgumentException("Erreur lors du décodage du fichier de mapping", e);
		}

		return document;
	}

	public void setMappingResources(String... mappingResources) {
		this.mappingResources = mappingResources;
	}

	public Importer buildImporter(String mappingId) {
		Element element = elementsMappedById.get(mappingId);
		return ImportBuilder.buildFrom(element);
	}

}
