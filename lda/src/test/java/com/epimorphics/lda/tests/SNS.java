/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.tests;

import com.epimorphics.lda.rdfq.Any;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.tests_support.ExpandOnly;
import com.epimorphics.lda.tests_support.MakeData;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public final class SNS extends ExpandOnly
	{
	public SNS(String expansions) 
		{ super( MakeData.modelForBrief( "bname" ), expansions ); }
	
	public SNS(String expansions, String others ) 
		{ super( MakeData.modelForBrief( "bname", others ), expansions ); }

	@Override public Resource normalizeResource( String s ) 
		{
		String u = expand( s );
		return ResourceFactory.createResource( u );
		}

	@Override public Any normalizeNodeToRDFQ( String prop, String val, String language ) 
		{
		return val.startsWith("?") ? RDFQ.var( val ) : RDFQ.literal( val );
		}
	}