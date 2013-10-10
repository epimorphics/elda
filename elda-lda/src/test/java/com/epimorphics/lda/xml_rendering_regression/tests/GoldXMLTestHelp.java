package com.epimorphics.lda.xml_rendering_regression.tests;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
    Class to hold details of an XML rendering gold test.
*/
public class GoldXMLTestHelp {
	
	private static final String goldRoot = "src/test/resources/xml_gold/";
	
	final Model objectModel;
	final Model metaModel;
	final boolean suppressIPTO;
	final String expected_xml;
	final String root_uri;
	final ShortnameService sns;
	
	GoldXMLTestHelp( String root_uri, Model objectModel, Model metaModel, ShortnameService sns, boolean suppress_IPTO, String expected_xml ) {
		this.root_uri = root_uri;
		this.objectModel = objectModel;
		this.metaModel = metaModel;
		this.expected_xml = expected_xml;
		this.suppressIPTO = suppress_IPTO;
		this.sns = sns;
	}
	
	static GoldXMLTestHelp load( String name ) {
		Model objectModel = EldaFileManager.get().loadModel( goldRoot + name + "/object_model.ttl" );
		Model metaModel = EldaFileManager.get().loadModel( goldRoot + name + "/meta_model.ttl" );
		boolean suppressIPTO = readBoolean( goldRoot + name + "/suppress_ipto.bool" );
		String root_uri = readLine(  goldRoot + name + "/root.uri" );
		String expected_xml = EldaFileManager.get().readWholeFileAsUTF8( goldRoot + name + "/xml-rendering.xml" );
		ShortnameService sns = readShortnames( goldRoot + name + "/names.sns" );
		return new GoldXMLTestHelp( root_uri, objectModel, metaModel, sns, suppressIPTO, expected_xml );
	}

	private static ShortnameService readShortnames( String fileName ) {
		Set<String> seen = new HashSet<String>();
		Model config = ModelFactory.createDefaultModel();
		for (String line: EldaFileManager.get().readWholeFileAsUTF8( fileName ).split( "\n" )) {
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
		String all = EldaFileManager.get().readWholeFileAsUTF8( fileName );
		return all.replaceFirst( "\n.*", "" );
	}
}