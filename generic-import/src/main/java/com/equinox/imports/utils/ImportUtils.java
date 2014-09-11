package com.equinox.imports.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ImportUtils {

	public static Document createDocument(String contenuFichier) throws SAXException, IOException,
			ParserConfigurationException {
		// Création du document XML
		Document toReturn = null;

		// création d'une fabrique de documents
		DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

		// création d'un constructeur de documents
		DocumentBuilder constructeur = fabrique.newDocumentBuilder();
		toReturn = constructeur.parse(new ByteArrayInputStream(contenuFichier.getBytes()));
		return toReturn;
	}

	public static String lireFichier(String fileName) throws IOException {
		StringBuilder content = new StringBuilder();
		String line;

		FileReader f = new FileReader(fileName);
		BufferedReader reader = new BufferedReader(new BufferedReader(f));
		try {

			while ((line = reader.readLine()) != null) {
				content.append(line);
			}
			return content.toString();

		} finally {
			if (reader != null) {
				reader.close();
			}
			if (f != null) {
				f.close();
			}
		}

	}
}
