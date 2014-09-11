package com.equinox.imports.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.equinox.imports.ImportLine;
import com.equinox.imports.exception.ImportException;
import com.equinox.imports.exception.ParseFileImportException;
import com.equinox.imports.utils.ImportUtils;

public class XMLImportFile extends AbstractImportFile {

	private final String baseElement;

	public XMLImportFile(String id, String baseElement) {
		super(id);
		this.baseElement = baseElement;
	}

	@Override
	protected String extractFile(String[] files) throws IOException, ParseFileImportException {

		// On recherche le fichier qui contient la balise souhaitée
		String toReturn = null;
		for (String file : files) {
			Document document = getDocument(file);

			NodeList nodes = document.getElementsByTagName(this.baseElement);

			if (nodes != null && nodes.getLength() > 0) {
				toReturn = file;
				break;
			}
		}

		if (StringUtils.isEmpty(toReturn)) {
			throw new FileNotFoundException("Impossible de trouver de fichier avec la balise : " + this.baseElement);
		}

		return toReturn;
	}

	@Override
	protected List<ImportLine> buildLignesSpecifique(String file) throws ImportException {

		List<ImportLine> toReturn = new ArrayList<ImportLine>();

		Document document = getDocument(file);

		NodeList nodes = document.getElementsByTagName(this.baseElement);

		for (int i = 0; i < nodes.getLength(); i++) {

			Element element = (Element) nodes.item(i);

			ImportLine line = new ImportLine(this, Long.valueOf(i + 1));
			processLine(element, line, "");
			toReturn.add(line);
		}

		return toReturn;
	}

	private void processLine(Node node, ImportLine parent, String tree) {

		// on a un noeud de type texte : on va affecter la valeur du texte en
		// tant que contenu de l'arbre courant. Attention, à l'intérieur d'une
		// balise, on a un enfant de type TEXT pour chaque ligne. Donc si balise
		// ouvrante et fermante sont sur des lignes différentes, on a un enfant
		// TEXT vide ; si on a 3 sous-balises séparées par un saut de ligne, on
		// a 3 nouveaux enfants TEXT vides... On est donc obligés de ne pas
		// traiter les chaines vides. Si la chaine est vide, la méthode
		// retournera null afin de ne pas créer de sous-arbre vide au parent.
		if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
			if (StringUtils.isNotBlank(node.getNodeValue())) {
				parent.addColumn(tree, node.getNodeValue());
			}
		}

		// on a un noeud de type element : on récupère son nom, ses attributs et
		// ses enfants (c'est-à-dire ses sous-balises et son contenu)
		else if (node.getNodeType() == Node.ELEMENT_NODE) {

			NamedNodeMap attributs = node.getAttributes();
			for (int i = 0; i < attributs.getLength(); i++) {
				Node attr = attributs.item(i);
				parent.addColumn(tree + node.getNodeName() + "[" + attr.getNodeName() + "]", attr.getNodeValue());
			}

			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				ImportLine subLine = new ImportLine(this, parent.getNumber());
				processLine(child, subLine, tree + node.getNodeName() + "/");
			}

		}

	}

	private Document getDocument(String file) throws ParseFileImportException {
		try {
			return ImportUtils.createDocument(ImportUtils.lireFichier(file));
		} catch (IOException | SAXException | ParserConfigurationException e) {
			throw new ParseFileImportException("Erreur lors du parsage du fichier " + file + " : " + e.getMessage(),
					this, e);
		}
	}
}
