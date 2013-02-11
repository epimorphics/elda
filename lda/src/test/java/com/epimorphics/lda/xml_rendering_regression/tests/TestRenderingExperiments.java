package com.epimorphics.lda.xml_rendering_regression.tests;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestRenderingExperiments extends XMLTestCase {
	
	public void testIt() throws ParserConfigurationException, SAXException, IOException {
		Document d1 = parse("<outside> A <inside> B </inside> C </outside>");
		Document d2 = parse("<outside> A <inside> X </inside> C </outside>");
		assertXMLEqual( d1, d2 );
	}

	public Document parse( String data ) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse( new InputSource( new StringReader( data ) ) );
	}

}
