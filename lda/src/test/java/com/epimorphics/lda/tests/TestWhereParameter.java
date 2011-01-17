/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.shortnames.ShortnameService;

public class TestWhereParameter 
	{
	@Test public void testAddWhereParameter()
		{    
        ShortnameService sns = TestSelectParameter.makeSNS();
        APIQuery q = new APIQuery(sns);
        APIQuery.Param _select = APIQuery.Param.make( "_where" );
        String theBaseQuery = q.assembleSelectQuery( TestSelectParameter.noPrefixes );
        String theWhereClause = "?p <spoo:equals> 17";
        q.addFilterFromQuery( _select, theWhereClause );
    //
    // this is horrid -- want something better later. too dependent on
    // string arithmetic.
    //
        String theUpdatedQuery = q.assembleSelectQuery( TestSelectParameter.noPrefixes);
        assertTrue( theUpdatedQuery.contains( theWhereClause ) );
        // Commented out. The base case has a dumy ?x ?p ?o clause but this should not be present in the
        // case of a where clause
//		if (!theUpdatedQuery.replace( theWhereClause, "" ).equals( theBaseQuery ))
//			{
//			fail( "BOOM [FIX THIS MESSAGE]" );
//			}
		}
	}
