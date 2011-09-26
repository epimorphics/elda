/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.QueryArgumentsImpl;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests.FakeNamedViews;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestVariableSubstitutions {

	@Test public void testOrderByParameterExpandsVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "_orderBy={x}" );
		VarValues bindings = MakeData.variables( "x=ordering" );
		CallContext cc = CallContext.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = new APIQuery( sns );
		QueryArgumentsImpl qa = new QueryArgumentsImpl(aq);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq, qa );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		qa.updateQuery();
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		assertMatches( "(?s).*ORDER BY ordering.*", q );
	}
	
	@Test public void testOrderBySettingExpandsVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "" );
		VarValues bindings = MakeData.variables( "x=ordering" );
		CallContext cc = CallContext.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = new APIQuery( sns );
		aq.setOrderBy( "?x" );
		QueryArgumentsImpl qa = new QueryArgumentsImpl(aq);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq, qa );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		qa.updateQuery();
		String q = aq.assembleSelectQuery( cc, PrefixMapping.Factory.create() );
		assertMatches( "(?s).*ORDER BY 'ordering'.*", q );        
	}
	
	@Test public void testSelectExpandsVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "_select={x}" );
		VarValues bindings = MakeData.variables( "x=myQueryHere" );
		CallContext cc = CallContext.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = new APIQuery( sns );
		QueryArgumentsImpl qa = new QueryArgumentsImpl(aq);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq, qa );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		qa.updateQuery();
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		assertEquals( "myQueryHere OFFSET 0 LIMIT 10", q );
	}

	private void assertMatches( String pattern, String subject ) {
		if (!subject.matches( pattern ))
			fail( "subject does not match pattern '" + pattern + "': " + subject );
	}
}
