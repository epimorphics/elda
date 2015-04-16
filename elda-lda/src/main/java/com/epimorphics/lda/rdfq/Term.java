/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;

public abstract class Term extends Any 
	{
	public abstract Term replaceBy( String r );
	
	public abstract String spelling();
	
	@Override public StringBuilder renderWrapped( PrefixLogger pl, StringBuilder out )
		{ return out.append( asSparqlTerm( pl ) ); }
	}