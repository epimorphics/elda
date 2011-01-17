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
	}