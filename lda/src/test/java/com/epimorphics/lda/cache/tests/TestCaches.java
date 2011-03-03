package com.epimorphics.lda.cache.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.cache.Cache.Controller;
import com.epimorphics.lda.cache.LimitEntriesController;
import com.epimorphics.lda.cache.LimitTriplesController;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.sources.SourceBase;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

public class TestCaches
	{
	static class FakeSource extends SourceBase implements Source
		{
		final String name;
		
		public FakeSource(String name) 
			{ this.name = name;	}

		@Override public QueryExecution execute(Query query) 
			{ throw new RuntimeException( "should never be called" ); }

		@Override public void addMetadata(Resource meta) 
			{ throw new RuntimeException( "should never be called" ); }
		
		@Override public String toString() 
			{ return "FakeSource:" + name; }

		@Override public Lock getLock() {
			// TODO Auto-generated method stub
			return null;
			}
		}
	
	static final Resource RA = ResourceFactory.createResource( "eh:/A" );
	
	static final Resource RB = ResourceFactory.createResource( "eh:/B" );
	
	static final List<Resource> resources = CollectionUtils.list( RA, RB );
	
	@Test public void testLimitEntriesCache() 
		{
		Graph g = GraphTestBase.graphWith( "" );
		APIResultSet rs = new APIResultSet( g, resources, true );
		Source s = new FakeSource( "limited.entries" );
		Controller cm = new LimitEntriesController();
		Cache c = cm.cacheFor( s, "1" );
		assertEquals( 0, c.numEntries() );
		c.cacheDescription( resources, "view.string", rs );
		assertEquals( 1, c.numEntries() );
		c.cacheDescription( resources, "view.string.other", rs );
		assertEquals( 0, c.numEntries() );
		}
	
	@Test public void testLimitTriplesCache() 
		{
		Graph g = GraphTestBase.graphWith( "a P b; c P d" );
		APIResultSet rs = new APIResultSet( g, resources, true );
		Source s = new FakeSource( "limited.triples" );
		Controller cm = new LimitTriplesController();
		Cache c = cm.cacheFor( s, "2" );
		assertEquals( 0, c.numEntries() );
		c.cacheDescription( resources, "view.string", rs );
		assertEquals( 1, c.numEntries() );
		c.cacheDescription( resources, "view.string.other", rs );
		assertEquals( 0, c.numEntries() );
		}
	}
