/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static com.epimorphics.util.CollectionUtils.set;
import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.core.APIQuery;
import com.epimorphics.lda.shortnames.ShortnameService;

public class TestWhereParameter 
	{
	static final String defaultQuery = "?item ?__p ?__v .";
	
	@Test public void testAddWhereParameter()
		{    
        ShortnameService sns = TestSelectParameter.makeSNS();
        APIQuery q = new APIQuery(sns);
        APIQuery.Param _select = APIQuery.Param.make( "_where" );
        String theBaseQuery = q.assembleSelectQuery( TestSelectParameter.noPrefixes );
        String theWhereClause = "?p <spoo:equals> 17";
        q.addFilterFromQuery( _select, set(theWhereClause) );
    //
    // this is horrid -- want something better later. too dependent on
    // string arithmetic.
    //
        String theUpdatedQuery = q.assembleSelectQuery( TestSelectParameter.noPrefixes);
//        System.err.println( ">> the updated query: " + theUpdatedQuery );
//        System.err.println( ">> the base query: " + theBaseQuery );
        assertTrue( theUpdatedQuery.contains( theWhereClause ) );
        // Commented out. The base case has a dumy ?x ?p ?o clause but this should not be present in the
        // case of a where clause
		String pruned = theUpdatedQuery.replace( theWhereClause, "" ).replaceAll( "[ \n]", "" );
		String other = theBaseQuery.replace(defaultQuery, "").replaceAll( "[ \n]", "" );
		if (!pruned.equals( other))
			{
			fail( "the updated query " + theUpdatedQuery + " isn't just the base query with different triples." );
			}
		}
	}
