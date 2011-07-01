/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.tests;

import static com.epimorphics.util.CollectionUtils.set;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.ContextQueryUpdater;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.RenderExpression;
import com.epimorphics.lda.rdfq.Variable;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.tests_support.ShortnameFake;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestExistsModifier 
	{
	private static final class Shorts extends ShortnameFake 
		{
		static final String NS = "fake:";
		
		public Shorts(String brief) 
			{ super( MakeData.modelForBrief( brief ) ); }
		
		@Override public Resource normalizeResource( String shortName ) 
			{ return ResourceFactory.createResource( NS + shortName ); }

		@Override public Any normalizeNodeToRDFQ( String prop, String val, String language ) 
			{
			if (prop.equals( "type" )) return RDFQ.uri( NS + val );
			if (val.equals("true")) return RDFQ.literal( val );
			if (val.startsWith( "?" )) return RDFQ.var( val );
			throw new RuntimeException( "prop: " + prop + ", val: " + val );
			}
		}
	
	@Test public void testExists()
		{
		Shorts sns = new Shorts( "exists-backwards" );
		APIQuery q = new APIQuery( sns );
		ContextQueryUpdater x = new ContextQueryUpdater( (CallContext) null, NamedViews.noNamedViews, sns, q );
		x.addFilterFromQuery( Param.make( sns, "exists-backwards" ), set("true") );
		List<RDFQ.Triple> triples = q.getBasicGraphTriples();
		assertEquals( 1, triples.size() );
		RDFQ.Triple t = triples.get(0);
		assertEquals( RDFQ.var( "?item" ), t.S );
		assertEquals( RDFQ.uri( Shorts.NS + "backwards" ), t.P );
		assertTrue( t.O instanceof Variable );
		}
	
	@Test public void testNotExists()
		{
		Shorts sns = new Shorts( "exists-backwards" );
		APIQuery q = new APIQuery( sns );
		ContextQueryUpdater x = new ContextQueryUpdater( (CallContext) null, NamedViews.noNamedViews, sns, q );
		x.addFilterFromQuery( Param.make( sns, "exists-backwards" ), set("false") );
		List<RDFQ.Triple> triples = q.getBasicGraphTriples();
		List<RenderExpression> filters = q.getFilterExpressions();
	//
		assertEquals( "should be only one triple in pattern", 1, triples.size() );
		RDFQ.Triple t = triples.get(0);
	//
		assertEquals( RDFQ.var( "?item" ), t.S );
		assertEquals( RDFQ.uri( Shorts.NS + "backwards" ), t.P );
		assertTrue( t.O instanceof Variable );
		assertTrue( "result triple must be OPTIONAL", t.isOptional() );
	//
		assertEquals( "should be one filter expression", 1, filters.size() );
		assertEquals( RDFQ.apply("!", RDFQ.apply( "bound", t.O ) ), filters.get(0) );
		}
	
	static final Model model = MakeData.specModel
		( "fake:S fake:type fake:Item"
		+ "; fake:S fake:backwards 17"
		+ "; fake:T fake:type fake:Item" 
		);

	@Test public void testExistsBackwardsTrueFinds_FakeS()
		{
		testNotExistsXY( "true", "fake:S" );
		}
	
	@Test public void testExistsBackwardsFalseFinds_FakeT()
		{
		testNotExistsXY( "false", "fake:T" );
		}
	
	/**
	    Test that looking for items with fake type Item using
	    exists-backwards={existsSetting} will produce the single 
	    answer {expect}.
	*/
	public void testNotExistsXY( String existsSetting, String expect )
		{
		Shorts sns = new Shorts( "type,exists-backwards" );
		APIQuery q = new APIQuery( sns );		
		ContextQueryUpdater x = new ContextQueryUpdater( (CallContext) null, NamedViews.noNamedViews, sns, q );
		x.addFilterFromQuery( Param.make( sns, "type" ), set("Item") );
		x.addFilterFromQuery( Param.make( sns, "exists-backwards" ), set(existsSetting) );
	//
		String query = q.assembleSelectQuery( PrefixMapping.Factory.create() );
		QueryExecution qx = QueryExecutionFactory.create( query, model );
		ResultSet rs = qx.execSelect();
	//	
		Set<Resource> solutions = new HashSet<Resource>();
		while (rs.hasNext()) solutions.add( rs.next().getResource( "item" ) );
		assertEquals( CollectionUtils.a( resource( expect ) ), solutions );
		}

	private Resource resource( String uri ) 
		{ return ResourceFactory.createResource( uri ); }
	}
