/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query.tests;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.APIQuery.QueryBasis;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.rdfq.RDFQ.Triple;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.HereSource;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;

public class TestConstructsTextQueries {
	
	Model config = ModelFactory.createDefaultModel();
	
	Resource endpoint = config.createResource( "here:eh:/endpoint" );

	ShortnameService sns = new SNS( "" );

	@Test public void testConstructsSimpleQuery() {
		testConstructsSimpleSearchTriples( TextSearchConfig.JENA_TEXT_QUERY );	
	}
	
	@Test public void testConstructsSimpleConfiguredQuery() {
		Property queryProperty = ResourceFactory.createProperty( "eh:/query" );
		endpoint.addProperty( ELDA_API.textQueryProperty, queryProperty );
		testConstructsSimpleSearchTriples(queryProperty);		
	}

	private void testConstructsSimpleSearchTriples(Property queryProperty) {
		final Source s = new HereSource( config, endpoint );
		QueryBasis qb = new StubQueryBasis(sns) {

			@Override public TextSearchConfig getTextSearchConfig() { 
				return s.getTextSearchConfig(); 
			}
		};
	//
		APIQuery q = new APIQuery(qb);
		q.addSearchTriple( "target" );
	//
		Set<Triple> obtained = new HashSet<Triple>( q.getBasicGraphTriples() );
	//
		Set<Triple> expected = new HashSet<Triple>();
		expected.add( RDFQ.triple( RDFQ.var("?item"), RDFQ.uri(queryProperty), RDFQ.literal( "target" ) ) );
	//
		assertEquals( expected, obtained );
	}
	
	@Test public void testUsesConfiguredTextProperty() {
		testUsesConfiguredTextProperty( TextSearchConfig.JENA_TEXT_QUERY );
	}

	private void testUsesConfiguredTextProperty(Property jenaTextQuery) {
		Property contentProperty = ResourceFactory.createProperty( "eh:/content" );
		endpoint.addProperty( ELDA_API.textContentProperty, contentProperty );
		final Source s = new HereSource( config, endpoint );
		QueryBasis qb = new StubQueryBasis(sns) {

			@Override public TextSearchConfig getTextSearchConfig() { 
				return s.getTextSearchConfig(); 
			}
		};
	//
		APIQuery q = new APIQuery(qb);
		q.addSearchTriple( "target" );
	//
		Set<Triple> obtained = new HashSet<Triple>( q.getBasicGraphTriples() );
	//
		Set<Triple> expected = new HashSet<Triple>();
		AnyList searchOperand = RDFQ.list( RDFQ.uri(contentProperty), RDFQ.literal( "target" ) );
		expected.add( RDFQ.triple( RDFQ.var("?item"), RDFQ.uri( TextSearchConfig.JENA_TEXT_QUERY ), searchOperand ) );
	//
		assertEquals( expected, obtained );
	}

	@Test public void testUsesConfiguredTextOperand() {
		int number = 17;
		String searchString = "target";
	//
		Resource a = config.createProperty( "eh:/content" );
		Literal b = config.createLiteral( "?_search" );
		Literal c = config.createTypedLiteral( number );
	//
		Resource operand = config.createList( new RDFNode[] {a, b, c} );
		endpoint.addProperty( ELDA_API.textSearchOperand, operand );
		final Source s = new HereSource( config, endpoint );
		final TextSearchConfig tsc = s.getTextSearchConfig();
	//
		QueryBasis qb = new StubQueryBasis(sns) {

			@Override public TextSearchConfig getTextSearchConfig() { 
				return tsc; 
			}
		};
	//
		APIQuery q = new APIQuery(qb);
		q.addSearchTriple( searchString );
	//
		Set<Triple> obtained = new HashSet<Triple>( q.getBasicGraphTriples() );
	//
		Set<Triple> expected = new HashSet<Triple>();
		AnyList searchOperand = RDFQ.list( RDFQ.uri(a), RDFQ.literal( searchString ), RDFQ.literal(number) );
		expected.add( RDFQ.triple( RDFQ.var("?item"), RDFQ.uri( TextSearchConfig.JENA_TEXT_QUERY ), searchOperand ) );
	//
		assertEquals( expected, obtained );
	}
	
}
