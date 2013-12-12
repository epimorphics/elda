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

public class TestSharesPropertyVariables {

	@Test public void testShared() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "min-aname=1&max-aname=3" );
		Bindings bindings = MakeData.variables( "aname=17" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "aname=eh:/full-aname" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		int count = aq.countVarsAllocated();
		if (count != 1) {
			fail( "expected to allocate only one variable, but generated query was:\n" + q + "\nwith " + count + " variables." );
		}
	}
}
