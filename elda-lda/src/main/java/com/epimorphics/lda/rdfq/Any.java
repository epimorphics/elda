/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;

public abstract class Any implements RenderExpression
	{
	@Override public String toString()
		{ return "<!! " + asSparqlTerm( PrefixLogger.some() ) + "!!>"; }
	
	@Override public boolean equals( Object other )
		{ throw new UnsupportedOperationException(); }
	
	@Override public StringBuilder render( PrefixLogger pl, StringBuilder out )
		{ return out.append( asSparqlTerm( pl ) ); }
	
	@Override public StringBuilder renderWrapped( PrefixLogger pl, StringBuilder out )
		{ return render( pl, out.append( "(" ) ).append( ")" ); }
	
	public abstract String asSparqlTerm( PrefixLogger pl );
	}