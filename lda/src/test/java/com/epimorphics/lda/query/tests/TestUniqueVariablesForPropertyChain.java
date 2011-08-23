package com.epimorphics.lda.query.tests;

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
import com.epimorphics.util.Util;
import com.hp.hpl.jena.shared.PrefixMapping;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestUniqueVariablesForPropertyChain {

	@Test public void testShared() {
		MultiMap<String, String> qp = MakeData.parseQueryString( "first.aname=1&first.bname=2" );
		VarValues bindings = MakeData.variables( "" );
		CallContext cc = CallContext.createContext( Util.newURI("my:URI"), qp, bindings );
		NamedViews nv = new FakeNamedViews();
		ShortnameService sns = new SNS( "first=eh:/first;aname=eh:/full-aname;bname=eh:/full-bname", "first" );
		APIQuery aq = new APIQuery( sns );
		QueryArgumentsImpl qa = new QueryArgumentsImpl(aq);
		ContextQueryUpdater cq = new ContextQueryUpdater( cc, nv, sns, aq, qa );
		cq.updateQueryAndConstructView( aq.deferredFilters );
		qa.updateQuery();
		String q = aq.assembleSelectQuery( PrefixMapping.Factory.create() );
		int count = aq.countVarsAllocated();
		if (count != 1) {
			fail( "expected to allocate only one variable, but generated query was:\n" + q + "\nwith " + count + " variables." );
		}
	}
}
