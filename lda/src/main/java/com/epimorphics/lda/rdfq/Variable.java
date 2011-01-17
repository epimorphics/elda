/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

public class Variable extends Any 
	{
	final String name;
	
	public Variable( String name )
		{ this.name = name; }
	
	public String name()
		{ return name; }
	
	@Override public String asSparqlTerm()
		{ return name; }
	
	@Override public StringBuilder renderWrapped( StringBuilder out ) 
		{ return out.append( name ); }
	
	@Override public boolean isFinal() 
		{ return false; }
	}