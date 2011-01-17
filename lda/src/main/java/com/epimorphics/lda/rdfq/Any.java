/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

public abstract class Any implements RenderExpression
	{
	@Override public String toString()
		{ return "<!! " + asSparqlTerm() + "!!>"; }
	
	@Override public boolean equals( Object other )
		{ throw new UnsupportedOperationException(); }
	
	public StringBuilder render( StringBuilder out )
		{ return out.append( asSparqlTerm() ); }
	
	public StringBuilder renderWrapped( StringBuilder out )
		{ return render( out.append( "(" ) ).append( ")" ); }
	
	public abstract String asSparqlTerm();
	
	public abstract boolean isFinal();
	}