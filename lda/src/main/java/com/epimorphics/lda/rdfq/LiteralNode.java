/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.vocabulary.XSD;

public class LiteralNode extends Term 
	{
	final String spelling;
	final String language;
	final String datatype;
	
	public LiteralNode( String spelling ) 
		{ this( spelling, "", "" ); }
	
	public LiteralNode( String spelling, String language, String datatype ) 
		{ 
		this.spelling = spelling; 
		this.language = language == null ? "" : language; 
		this.datatype = datatype == null ? "" : datatype; 
		}
	
	@Override public String toString() 
		{
		return "{" + spelling + "|" + language + "|" + datatype + "}";
		}
	
	@Override public String asSparqlTerm( PrefixLogger pl )
		{ 
		String lf = "\"" + spelling.replaceAll( "\\\\", "\\\\" ) + "\"";
		// System.err.println( ">> DATATYPE: " + datatype );
		if (datatype.equals( XSD.integer.getURI() )) return spelling;
		if (datatype.equals( XSD.xint.getURI() )) return spelling;
		if (language.length() > 0) return lf + "@" + language;
		if (datatype.length() > 0) return lf + "^^<" + datatype + ">";
		return lf;
		}
	
	@Override public boolean isFinal() 
		{ return !spelling.contains( "{" ); }
	
	@Override public LiteralNode replaceBy( String r ) 
		{ return new LiteralNode( r, language, datatype ); }
	
	@Override public String spelling() 
		 { return spelling; }
	
	@Override public boolean equals( Object other )
		{ return other instanceof LiteralNode && same( (LiteralNode) other ); }
	
	private boolean same( LiteralNode other ) 
		{ 
		return 
			spelling.equals( other.spelling ) 
			&& language.equals( other.language ) 
			&& datatype.equals( other.datatype )
			; 
		}
	}