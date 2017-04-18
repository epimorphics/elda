/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import java.util.*;

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

	@Test  public void testShared() {
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
	
	@Test public void testSharedSortVariables() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "" );
		Bindings bindings = MakeData.variables( "" );
		Bindings cc = Bindings.createContext( bindings, qp );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "p1=eh:/p1;p2=eh:/p2" );
		APIQuery aq = QueryTestUtils.queryFromSNS(sns);
		aq.setSortBy("p1,p1.p2");
		ContextQueryUpdater cq = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, cc, nv, sns, aq );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		
		// should generate query like:
		// SELECT DISTINCT ?item
		// 	WHERE {
		//		?item ?__p ?__v .
		//		OPTIONAL { ?item <eh:/p1> ?___0 . }
		//		OPTIONAL { ?___0 <eh:/p2> ?___1 . }
		//		}  ORDER BY  ?___0  ?___1  ?item OFFSET 0 LIMIT 10

		List<String> opts = new ArrayList<String>();
		List<String> lines = Arrays.asList(q.split("\n"));
		for (String line: lines)
			if (line.startsWith("OPTIONAL"))
				opts.add(line);
		assertEquals(2, opts.size());
		
		String A = opts.get(0), B = opts.get(1);
		String opt0 = "OPTIONAL { ?item <eh:/p1> ?___0 . }";
		String opt1 = "OPTIONAL { ?___0 <eh:/p2> ?___1 . }";
		if (A.equals(opt0) && B.equals(opt1)) return;
		if (A.equals(opt1) && B.equals(opt0)) return;
		
		fail("generated query does not reuse ?___0");
		
	}
	
}
