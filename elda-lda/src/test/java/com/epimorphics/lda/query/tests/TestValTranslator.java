/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.VarSupply;
import com.epimorphics.lda.query.ValTranslator;
import com.epimorphics.lda.query.ValTranslator.Filters;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestValTranslator {
	
	final Filters absentFilters = null;
	
	final VarSequence absentSupply = null;
	
	final PrefixMapping noPrefixes = PrefixMapping.Factory.create().lock();
	
	@Test public void testUntypedLiteral() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( (String) null, "val", null );
		assertEquals( RDFQ.literal( "val" ), o );
	}
	
	@Test public void testResourceLiteral() {
		testResourceObject(RDFS.Resource.getURI());
		testResourceObject(OWL.Thing.getURI());
	}

	private void testResourceObject(String type) {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( type, "my:uri", null );
		assertEquals( RDFQ.uri( "my:uri" ), o );
	}
	
	@Test public void testDatatypedObject() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'. :dt a rdfs:Datatype." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		String NS = root.getModel().expandPrefix( ":" );
		Any o = vt.objectForValue( NS + "dt", "lexicalForm", null );
		assertEquals( RDFQ.literal( "lexicalForm", "", NS + "dt" ), o );		
	}
	
	@Test public void testSimpleLiteralObjectWithoutLanguage() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( API.SimpleLiteral.getURI(), "lexicalForm", null );
		assertEquals( RDFQ.literal( "lexicalForm", "", "" ), o );		
	}
	
	@Test public void testSimpleLiteralObjectWithLanguage() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( API.SimpleLiteral.getURI(), "lexicalForm", "fr" );
		assertEquals( RDFQ.literal( "lexicalForm", "", "" ), o );		
	}
	
	@Test public void testPlainLiteralObjectWithoutLanguage() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( API.PlainLiteral.getURI(), "lexicalForm", null );
		assertEquals( RDFQ.literal( "lexicalForm", "", "" ), o );		
	}
	
	@Test public void testPlainLiteralObjectWithLanguage() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( API.PlainLiteral.getURI(), "lexicalForm", "en" );
		assertEquals( RDFQ.literal( "lexicalForm", "en", "" ), o );		
	}
	
	@Test public void testObjectTypedObjectWithoutExpansion() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		Any o = vt.objectForValue( "some:ObjectType", "likeURI", "en" );
		assertEquals( RDFQ.uri( "likeURI" ), o );		
	}
	
	@Test public void testObjectTypedObjectWithExpansion() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		ValTranslator vt = new ValTranslator(absentSupply, absentFilters, sns);
		String NS = root.getModel().expandPrefix( ":" );
		Any o = vt.objectForValue( "some:ObjectType", "val", "en" );
		assertEquals( RDFQ.uri( NS + "thing" ), o );		
	}
	
	@Test public void testMultipleLanguages() {
		Model spec = ModelIOUtils.modelFromTurtle( ":thing api:label 'val'." );
		Resource root = spec.createResource( "http://example.com/root" );
		ShortnameService sns = new StandardShortnameService( root, noPrefixes, null );
	//
		FilterList filters = new FilterList();
		ValTranslator vt = new ValTranslator( new VarSequence(), filters, sns);
		Any o = vt.objectForValue( (String) null, "val", "en,fr" );
		assertEquals( "?v_1", o.asSparqlTerm(null) );
		assertEquals( 1, filters.elements.size() );
		StringBuilder sb = new StringBuilder();
		filters.elements.get(0).render(null, sb);
		String expect = "(str(?v_1) = \"val\") && ((lang(?v_1) = \"en\") || (lang(?v_1) = \"fr\"))";
		assertEquals( expect, sb.toString() );
	}
	
	static class VarSequence implements VarSupply {

		int count = 0;
		
		@Override public Variable newVar() {
			return RDFQ.var( "?v_" + ++count );
		}
	}
	
	static class FilterList implements Filters {

		List<RenderExpression> elements = new ArrayList<RenderExpression>();
		
		@Override public void add(RenderExpression e) {
			elements.add( e );			
		}
	}

}
