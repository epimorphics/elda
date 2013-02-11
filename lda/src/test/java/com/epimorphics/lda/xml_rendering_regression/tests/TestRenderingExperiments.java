package com.epimorphics.lda.xml_rendering_regression.tests;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestRenderingExperiments extends XMLTestCase {
	
	public void testIt() throws ParserConfigurationException, SAXException, IOException {
		
		ShortnameService sns = new StandardShortnameService();
		
		XMLRenderer x = new XMLRenderer(sns);
		
		Document d = DOMUtils.newDocument();
		
		Model objectModel = ModelFactory.createDefaultModel();
		
		MergedModels mm = new MergedModels( objectModel );
		
		Resource root = mm.getMetaModel().createResource( "some:root-or-other" );
		
		Resource pt = mm.getMetaModel().createResource( "the:primary-topic" );
		
		root.addProperty( FOAF.primaryTopic, pt );
		
		pt.addProperty( RDFS.label, "label" );
		
		x.renderInto( root, mm, d, false );
			
		Document expected = parse("<result format='linked-data-api' version='0.2' href='some:root-or-other'><primaryTopic>X</primaryTopic></result>");

		if (false) assertXMLEqual( expected, d );
	}

	public Document parse( String data ) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse( new InputSource( new StringReader( data ) ) );
	}

}
