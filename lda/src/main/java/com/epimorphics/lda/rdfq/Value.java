/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.NodeFactory;

public class Value extends Term 
	{
	final String spelling;
	final String language;
	final String datatype;
	
	public static final Value emptyPlain = new Value("");
	
	public Value( String spelling ) 
		{ this( spelling, "", "" ); }
	
	public Value( String spelling, String language, String datatype ) 
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
		// System.err.println( ">> aST: " + spelling + "@" + language );
		String lang = (language.equals("none") ? "" : language);
		Node n = NodeFactory.createLiteralNode( spelling, lang, datatype );
		if (datatype.length() > 0) pl.present( datatype );
		String lf = FmtUtils.stringForNode( n, RDFUtils.noPrefixes ); 
		return lf;
		}
	
	@Override public Value replaceBy( String r ) 
		{ return new Value( r, language, datatype ); }
	
	@Override public String spelling() 
		 { return spelling; }
	
	public String type()
		{ return datatype; }
	
	@Override public boolean equals( Object other )
		{ return other instanceof Value && same( (Value) other ); }
	
	private boolean same( Value other ) 
		{ 
		return 
			spelling.equals( other.spelling ) 
			&& language.equals( other.language ) 
			&& datatype.equals( other.datatype )
			; 
		}
	}