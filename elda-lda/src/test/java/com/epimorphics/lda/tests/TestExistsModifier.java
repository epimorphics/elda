/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.Param;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.tests.QueryTestUtils;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestExistsModifier 
	{
	private static final class Shorts extends ExpandOnly 
		{
		static final String NS = "fake:";
		
		public Shorts( String intBrief ) 
			{ this( intBrief, "" ); }
		
		public Shorts( String intBrief, String otherBrief ) 
			{ super( MakeData.modelForBrief( intBrief, otherBrief ), "Item=fake:Item" ); }
		
		@Override public Resource asResource( String shortName ) 
			{ return ResourceFactory.createResource( NS + shortName ); }
		}
	
	@Test public void testExists()
		{
		Shorts sns = new Shorts( "backwards" );
		APIQuery q = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q );
		x.addFilterFromQuery( Param.make( sns, "exists-backwards" ), "true" );
		List<RDFQ.Triple> triples = q.getBasicGraphTriples();
		assertEquals( 1, triples.size() );
		RDFQ.Triple t = triples.get(0);
		assertEquals( RDFQ.var( "?item" ), t.S );
		assertEquals( RDFQ.uri( Shorts.NS + "backwards" ), t.P );
		assertTrue( t.O instanceof Variable );
		}
	
	@Test public void testNotExists()
		{
		Shorts sns = new Shorts( "backwards" );
		APIQuery q = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q );
		x.addFilterFromQuery( Param.make( sns, "exists-backwards" ), "false" );		
		List<RDFQ.Triple> triples = q.getBasicGraphTriples();
		List<List<RDFQ.Triple>> optionals = q.getOptionalGraphTriples();
		List<RenderExpression> filters = q.getFilterExpressions();
	//
		assertEquals( "should be no mandatory triples in pattern", 0, triples.size() );
		assertEquals( "should be one optional chain", 1, optionals.size() );
		assertEquals( "the single optional chain should have one element", 1, optionals.get(0).size() );
		RDFQ.Triple t = optionals.get(0).get(0);
	//
		assertEquals( RDFQ.var( "?item" ), t.S );
		assertEquals( RDFQ.uri( Shorts.NS + "backwards" ), t.P );
		assertTrue( t.O instanceof Variable );
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
		Shorts sns = new Shorts( "backwards", "type" );
		// System.err.println( ">> info: " + sns.asContext().getPropertyByName("type" ).getType() ) ;
		APIQuery q = QueryTestUtils.queryFromSNS(sns);		
		ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q );
		Param ptype = Param.make( sns, "type" );
		x.addFilterFromQuery( ptype, "Item" );
		x.addFilterFromQuery( Param.make( sns, "exists-backwards" ), existsSetting );
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
