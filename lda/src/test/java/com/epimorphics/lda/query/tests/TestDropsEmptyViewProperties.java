/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.query.APIQuery.Deferred;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.ExpansionPoints;
import com.epimorphics.lda.query.QueryArgumentsImpl;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.Couple;

public class TestDropsEmptyViewProperties {

	@Test public void ensureEmpytPropertiesIgnored() {
		NamedViews nv = NamedViews.noNamedViews;
		SNS sns = new SNS("a=eh:/A");
		CallContext cc = CallContext.createContext( null, MakeData.parseQueryString("_properties=,a," ), new VarValues() );
		APIQuery aq = new APIQuery( sns );
		ContextQueryUpdater cu = new ContextQueryUpdater
			( ContextQueryUpdater.ListEndpoint 
			, cc
			, nv
			, sns 
			, (ExpansionPoints) null 
			, new QueryArgumentsImpl( aq )
			); 
		Couple<View, String> ans = cu.updateQueryAndConstructView( new ArrayList<Deferred>() );
		assertEquals( CollectionUtils.set( new PropertyChain( "eh:/A" ) ), ans.a.chains() );
	}
}
