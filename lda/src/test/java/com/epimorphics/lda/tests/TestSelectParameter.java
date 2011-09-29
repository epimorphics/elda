/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.query.ContextQueryUpdater;
import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestSelectParameter 
    {
	@Test public void testMe()
        {    
        ShortnameService sns = makeSNS();
        APIQuery q = new APIQuery(sns);	
        ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q, q );
        String theSelectQuery = "this is a select query";
        x.handleReservedParameters( null, null, "_select", theSelectQuery );
        assertEquals( theSelectQuery + " OFFSET 0 LIMIT 10", q.assembleSelectQuery( RDFUtils.noPrefixes) );
        }
    
    @Test public void testCloneIncludesFixedQuery()
        {    
        ShortnameService sns = makeSNS();
        APIQuery q = new APIQuery(sns);	
        ContextQueryUpdater x = new ContextQueryUpdater( ContextQueryUpdater.ListEndpoint, (Bindings) null, NamedViews.noNamedViews, sns, q, q );
        String theSelectQuery = "this is a select query";
        x.handleReservedParameters( null, null, "_select", theSelectQuery );
        APIQuery cloned = q.clone();
        assertEquals( theSelectQuery + " OFFSET 0 LIMIT 10", cloned.assembleSelectQuery( RDFUtils.noPrefixes ) );
        }

    public static ShortnameService makeSNS()
        {
    	return new ShortnameService() 
    		{			
			@Override public Resource asResource(String s)
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public Resource asResource(RDFNode r) 
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public String expand(String s)
				{ throw new RuntimeException( "I wasn't expecting to be called." );	}
			
			@Override public Context asContext() 
		 		{ throw new RuntimeException( "I wasn't expecting to be called." );	}

			@Override public Any valueAsRDFQ(String prop, String val, String language) 
				{ throw new RuntimeException( "I wasn't expecting to be called." ); }

			@Override public NameMap nameMap() 
				{ throw new RuntimeException( "I wasn't expecting to be called." ); }

    		};
        }
    }
