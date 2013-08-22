package com.epimorphics.lda.sources.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.sources.*;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.util.FileManager;

public class TestSourceConfig {
	
	protected FileManager fm = FileManager.get();
	protected AuthMap am = new AuthMap();
	protected Model model = ModelFactory.createDefaultModel();
	
	@Test public void testMinimalConfig() {
		testClassMatchesMagicConfig( SparqlSource.class, "eh:/sparql" );
	}

	private void testClassMatchesMagicConfig(Class<? extends Source> type, String endpointString) {
		Source s = sourceForConfig(endpointString);
		JenaTestBase.assertInstanceOf( type, s );
	}

	private Source sourceForConfig(String endpointString) {
		Resource config = model.createResource( "eh:/spec" );
		Resource endpoint = model.createResource( endpointString );
		config.addProperty( API.sparqlEndpoint, endpoint );
		return GetDataSource.sourceFromSpec( fm, config, am );
	}
	
	@Test public void testTextSearchDefaults() {		
		Source s = sourceForConfig("eh:/sparql");
		assertEquals( Source.JENA_TEXT_QUERY, s.getTextQueryProperty() );
	}
	
	@Test public void testTextSearchFromConfig() {		
		Resource config = model.createResource( "eh:/spec" );
		Resource endpoint = model.createResource( "eh:/sparql" );
		config.addProperty( API.sparqlEndpoint, endpoint );
		Property textQuery = model.createProperty( "eh:/textQuery" );
		endpoint.addProperty( EXTRAS.textQueryProperty, textQuery );
		Source s = GetDataSource.sourceFromSpec( fm, config, am );
		assertEquals( textQuery, s.getTextQueryProperty() );
	}
	

}
