/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

public class Apply implements RenderExpression
	{
	private final String f;
	private final RenderExpression x;
	
	public Apply( String f, RenderExpression x ) 
		{ this.f = f; this.x = x; }
	
	@Override public StringBuilder render( StringBuilder out ) 
		{
		out.append( f );
		out.append( "(" );
		x.render( out );
		out.append( ")" );
		return out;
		}
	
	@Override public StringBuilder renderWrapped( StringBuilder out ) 
		{ return render( out );	}
	
	@Override public int hashCode() 
		{ return f.hashCode() ^ x.hashCode(); }
	
	@Override public boolean equals( Object other )
		{ return other instanceof Apply && same( (Apply) other ); }
	
	private boolean same( Apply other ) 
		{ return f.equals( other.f ) && x.equals( other.x ); }

	@Override public String toString()
		{
		StringBuilder b = new StringBuilder();
		render( b );
		return b.toString();
		}
	}