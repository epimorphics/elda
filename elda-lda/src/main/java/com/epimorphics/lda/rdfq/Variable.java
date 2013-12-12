/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;

public class Variable extends Any 
	{
	final String name;
	
	public Variable( String name )
		{ this.name = name; }
	
	public String name()
		{ return name; }
	
	@Override public boolean equals( Object other )
		{ return other instanceof Variable && name.equals( ((Variable) other).name ); }
	
	@Override public int hashCode()
		{ return name.hashCode(); }
	
	@Override public String asSparqlTerm( PrefixLogger pl )
		{ return name; }
	
	@Override public StringBuilder renderWrapped( PrefixLogger pl, StringBuilder out ) 
		{ return out.append( name ); }
	}