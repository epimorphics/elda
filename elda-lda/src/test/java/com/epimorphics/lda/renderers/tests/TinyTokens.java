/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
    Tiny tokeniser for tiny dom-expression parser for testing.
 	
 	@author chris
*/
public class TinyTokens 
	{
	protected String spelling;
	protected TinyTokens.Type type;
	private final Matcher m;
	
	static final Pattern p = Pattern.compile( "\\(|\\)|'[^']*'|[^()=' ]+=[^()=' ]+|[^()=' ]+|." );

	enum Type {WRONG, EOF, LPAR, RPAR, LIT, WORD, ATTR}
	
	public TinyTokens( String source ) 
		{
		this.m = p.matcher( source );
		advance();
		}
	
	public void advance()
		{
		if (m.find()) 
			{
			spelling = m.group();
			if (spelling.equals( " "))
				advance();
			else
				type =
					spelling.equals( "(" ) ? Type.LPAR
					: spelling.equals( ")" ) ? Type.RPAR
					: spelling.startsWith( "'" ) ? Type.LIT
					: spelling.contains( "=" ) ? Type.ATTR
					: Type.WORD
					;
			}
		else
			{
			spelling = "";
			type = Type.EOF;
			}
		}

	public void demand( TinyTokens.Type t ) 
		{
		if (type == t) advance();
		else throw new RuntimeException( "expected a " + t + " but got: " + type + " " + spelling );
		}
	}