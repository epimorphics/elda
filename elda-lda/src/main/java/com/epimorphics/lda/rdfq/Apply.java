/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;

public class Apply implements RenderExpression
	{
	private final String f;
	private final RenderExpression x;
	
	public Apply( String f, RenderExpression x ) 
		{ this.f = f; this.x = x; }
	
	@Override public StringBuilder render( PrefixLogger pl, StringBuilder out ) 
		{
		out.append( f );
		out.append( "(" );
		x.render( pl, out );
		out.append( ")" );
		return out;
		}
	
	@Override public StringBuilder renderWrapped( PrefixLogger pl, StringBuilder out ) 
		{ return render( pl, out );	}
	
	@Override public int hashCode() 
		{ return f.hashCode() ^ x.hashCode(); }
	
	@Override public boolean equals( Object other )
		{ return other instanceof Apply && same( (Apply) other ); }
	
	private boolean same( Apply other ) 
		{ return f.equals( other.f ) && x.equals( other.x ); }

	@Override public String toString()
		{
		StringBuilder b = new StringBuilder();
		render( PrefixLogger.some(), b );
		return b.toString();
		}
	}