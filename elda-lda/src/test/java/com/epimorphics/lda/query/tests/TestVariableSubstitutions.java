/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.tests.FakeNamedViews;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestVariableSubstitutions {

	@Test public void testOrderByParameterExpandsVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "_orderBy={x}" );
		Bindings bindings = MakeData.variables( "x=ordering" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		assertMatches( "(?s).*ORDER BY ordering.*", q );
	}
	
	@Test public void testOrderBySettingExpandsVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "" );
		Bindings bindings = MakeData.variables( "x=ordering" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		aq.setOrderBy( "?x" );
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( cc, PrefixMapping.Factory.create() );
		assertMatches( "(?s).*ORDER BY \"ordering\".*", q );        
	}
	
	@Test public void testSelectExpandsVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "_select={x}" );
		Bindings bindings = MakeData.variables( "x=myQueryHere" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		assertEquals( "myQueryHere OFFSET 0 LIMIT 10", q );
	}

	private void assertMatches( String pattern, String subject ) {
		if (!subject.matches( pattern ))
			fail( "subject does not match pattern '" + pattern + "': " + subject );
	}
}
