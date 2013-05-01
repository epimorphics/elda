package com.epimorphics.lda.xml_rendering_regression.tests;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
    Class to hold details of an XML rendering gold test.
*/
public class GoldXMLTest {
	
	private static final String goldRoot = "src/test/resources/xml_gold/";
	
	final Model objectModel;
	final Model metaModel;
	final boolean suppressIPTO;
	final String expected_xml;
	final String root_uri;
	final ShortnameService sns;
	
	GoldXMLTest( String root_uri, Model objectModel, Model metaModel, ShortnameService sns, boolean suppress_IPTO, String expected_xml ) {
		this.root_uri = root_uri;
		this.objectModel = objectModel;
		this.metaModel = metaModel;
		this.expected_xml = expected_xml;
		this.suppressIPTO = suppress_IPTO;
		this.sns = sns;
	}
	
	static GoldXMLTest load( String name ) {
		Model objectModel = FileManager.get().loadModel( goldRoot + name + "/object_model.ttl" );
		Model metaModel = FileManager.get().loadModel( goldRoot + name + "/meta_model.ttl" );
		boolean suppressIPTO = readBoolean( goldRoot + name + "/suppress_ipto.bool" );
		String root_uri = readLine(  goldRoot + name + "/root.uri" );
		String expected_xml = FileManager.get().readWholeFileAsUTF8( goldRoot + name + "/xml-rendering.xml" );
		ShortnameService sns = readShortnames( goldRoot + name + "/names.sns" );
		return new GoldXMLTest( root_uri, objectModel, metaModel, sns, suppressIPTO, expected_xml );
	}

	private static ShortnameService readShortnames( String fileName ) {
		Set<String> seen = new HashSet<String>();
		Model config = ModelFactory.createDefaultModel();
		for (String line: FileManager.get().readWholeFileAsUTF8( fileName ).split( "\n" )) {
			int eq = line.indexOf( '=' );
			String name = line.substring(0, eq), uri = line.substring(eq + 1);
			if (seen.add( name )) config.add( config.createResource( uri ), API.label, name );
		}
		return new StandardShortnameService( config );
	}

	private static boolean readBoolean( String fileName ) {
		String line = readLine( fileName );
		return line.equals( "true" );
	}

	private static String readLine( String fileName ) {
		String all = FileManager.get().readWholeFileAsUTF8( fileName );
		return all.replaceFirst( "\n.*", "" );
	}
}