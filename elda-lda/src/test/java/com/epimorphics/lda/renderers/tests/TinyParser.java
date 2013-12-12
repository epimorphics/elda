/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers.tests;

import org.w3c.dom.*;

import com.epimorphics.util.DOMUtils;

/**
    TinyParser constructs DOM tress from S-expression-like
    strings.
    
	@author eh
*/
public class TinyParser
	{
	protected Node parse( String source ) 
		{
		Document d = DOMUtils.newDocument();
		return parse( d, new TinyTokens( source ) );		
		}
	
	private Node parse( Document d, TinyTokens t ) 
		{
		if (t.type == TinyTokens.Type.LPAR)
			{
			t.advance();
			String tag = parseWord( d, t );
			Element e = d.createElement( tag );
			while (true)
				{
				switch (t.type)
					{
					case ATTR:
						int eq = t.spelling.indexOf( '=' );
						String name = t.spelling.substring( 0, eq );
						String value = t.spelling.substring( eq + 1 );
						e.setAttribute( name, value );
						t.advance();
						break;
						
					case LPAR:
					case LIT:
						e.appendChild( parse( d, t ) );
						break;
						
					case RPAR:
						t.advance();
						return e;
						
					default: 
						throw new RuntimeException( "Not allowed in element: " + t.type + " " + t.spelling );
					}
				}
			}
		else if (t.type == TinyTokens.Type.LIT)
			{
			try { return d.createTextNode( t.spelling.substring(1, t.spelling.length() - 1) ); } finally { t.advance(); }
			}
		else
			throw new RuntimeException( "bad token for parse: " + t.type + " " + t.spelling );
		}

	private String parseWord(Document d, TinyTokens t)
		{
		if (t.type == TinyTokens.Type.WORD)
			{
			try { return t.spelling; } finally { t.advance(); }
			}
		else
			throw new RuntimeException( "tag following LPAR must be WORD: " + t.type + " " + t.spelling );
		}
	}