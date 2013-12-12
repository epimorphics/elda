/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query.tests;

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

public class TestFixedSelectOffsetAndLimit {

	@Test public void ensureOffsetAndLimitApplyToFixedSelect() {
		ensureOffsetAndLimit( "SELECTION OFFSET 100 LIMIT 10", "_select=SELECTION&_page=10" );
		ensureOffsetAndLimit( "SELECTION OFFSET 20 LIMIT 2", "_select=SELECTION&_page=10&_pageSize=2" );
		ensureOffsetAndLimit( "SELECTION OFFSET 21 LIMIT 3", "_select=SELECTION&_page=7&_pageSize=3" );
	}

	private void ensureOffsetAndLimit(String expected, String queryArgs) {
		MultiMap<String, String> qp = MakeData.parseQueryString( queryArgs );
		Bindings bindings = MakeData.variables( "" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		assertMatches( expected, q );
	}

	private void assertMatches(String pattern, String subject) {
		if (!subject.matches( pattern ))
			fail( "subject does not match pattern '" + pattern + "': " + subject );
	}
}
