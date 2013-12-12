/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.query.tests.QueryTestUtils;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.util.RDFUtils;

public class TestSelectParameter 
    {
	@Test public void testMe()
        {    
        ShortnameService sns = new StandardShortnameService();
        APIQuery q = QueryTestUtils.queryFromSNS(sns);	
        ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q );
        String theSelectQuery = "this is a select query";
        x.handleReservedParameters( null, null, "_select", theSelectQuery );
        assertEquals( theSelectQuery + " OFFSET 0 LIMIT 10", q.assembleSelectQuery( RDFUtils.noPrefixes) );
        }
    
    @Test public void testCloneIncludesFixedQuery()
        {    
        ShortnameService sns = new StandardShortnameService();
        APIQuery q = QueryTestUtils.queryFromSNS(sns);	
        ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q );
        String theSelectQuery = "this is a select query";
        x.handleReservedParameters( null, null, "_select", theSelectQuery );
        APIQuery cloned = q.copy();
        assertEquals( theSelectQuery + " OFFSET 0 LIMIT 10", cloned.assembleSelectQuery( RDFUtils.noPrefixes ) );
        }
    }
