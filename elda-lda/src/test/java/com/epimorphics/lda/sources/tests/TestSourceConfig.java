package com.epimorphics.lda.sources.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.sources.*;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.test.JenaTestBase;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

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
		assertEquals( TextSearchConfig.JENA_TEXT_QUERY, s.getTextSearchConfig().getTextQueryProperty() );
	}
	
	@Test public void testTextSearchFromConfig() {		
		Resource config = model.createResource( "eh:/spec" );
		Resource endpoint = model.createResource( "eh:/sparql" );
		config.addProperty( API.sparqlEndpoint, endpoint );
		Property textQuery = model.createProperty( "eh:/textQuery" );
		endpoint.addProperty( EXTRAS.textQueryProperty, textQuery );
		Source s = GetDataSource.sourceFromSpec( fm, config, am );
		TextSearchConfig tsc = s.getTextSearchConfig();
		assertEquals( textQuery, tsc.getTextQueryProperty() );
		assertNull( tsc.getTextSearchOperand() );
	}
	
	@Test public void testDefaultTextProperty() {
		Resource config = model.createResource( "eh:/spec" );
		Resource endpoint = model.createResource( "eh:/sparql" );
		config.addProperty( API.sparqlEndpoint, endpoint );
		Source s = GetDataSource.sourceFromSpec( fm, config, am );
		TextSearchConfig tsc = s.getTextSearchConfig();
		assertEquals( RDFS.label, tsc.getTextContentProperty() );
		assertNull( tsc.getTextSearchOperand() );
	}
	
	@Test public void testTextPropertyFromConfig() {
		Resource config = model.createResource( "eh:/spec" );
		Resource endpoint = model.createResource( "eh:/sparql" );
		Property textContent = model.createProperty( "eh:/textContent" );
		endpoint.addProperty( EXTRAS.textContentProperty, textContent );
		config.addProperty( API.sparqlEndpoint, endpoint );
		Source s = GetDataSource.sourceFromSpec( fm, config, am );
		TextSearchConfig tsc = s.getTextSearchConfig();
		assertEquals( textContent, tsc.getTextContentProperty() );
		assertNull( tsc.getTextSearchOperand() );
	}
	
	// the search operand coming out should match the config list coming in.
	@Test public void testTextSearchListOperand() {
		Resource config = model.createResource( "eh:/spec" );
		Resource endpoint = model.createResource( "eh:/sparql" );
	//
		Resource a = model.createResource( "eh:/a" );
		Literal b = model.createLiteral( "b" );
		Literal c = model.createLiteral( "?c" );
	//
		Resource operand = model.createList( new RDFNode[] {a, b, c} );
	//
		endpoint.addProperty( EXTRAS.textSearchOperand, operand );		
		config.addProperty( API.sparqlEndpoint, endpoint );
		Source s = GetDataSource.sourceFromSpec( fm, config, am );
	//
		Any qA = RDFQ.uri( a.getURI() );
		Any qB = RDFQ.literal( b.getLexicalForm() );
		Any qC = RDFQ.literal( c.getLexicalForm() );
	//
		AnyList expected = RDFQ.list( qA, qB, qC );
		assertEquals( expected, s.getTextSearchConfig().getTextSearchOperand() );		
	}
	
}
