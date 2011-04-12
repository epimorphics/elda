/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

public class Infix implements RenderExpression
	{
	private final RenderExpression L, R;
	private final String op;
	
	public Infix( RenderExpression L, String op, RenderExpression R )
		{ this.L = L; this.op = op; this.R = R; }
	
	@Override public StringBuilder render( StringBuilder out ) 
		{
		L.renderWrapped( out );
		out.append( " " ).append( op ).append( " " );
		R.renderWrapped( out );
		return out;
		}
	
	@Override public StringBuilder renderWrapped( StringBuilder out ) 
		{
		out.append( "(" );
		render( out );
		out.append( ")" );
		return out;
		}
	}