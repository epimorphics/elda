/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;

public class Infix implements RenderExpression
	{
	private final RenderExpression L, R;
	private final String op;
	
	public Infix( RenderExpression L, String op, RenderExpression R )
		{ this.L = L; this.op = op; this.R = R; }
	
	@Override public StringBuilder render( PrefixLogger pl, StringBuilder out ) 
		{
		L.renderWrapped( pl, out );
		out.append( " " ).append( op ).append( " " );
		R.renderWrapped( pl, out );
		return out;
		}
	
	@Override public StringBuilder renderWrapped( PrefixLogger pl, StringBuilder out ) 
		{
		out.append( "(" );
		render( pl, out );
		out.append( ")" );
		return out;
		}
	}