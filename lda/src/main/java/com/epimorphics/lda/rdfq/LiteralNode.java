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
		Node n = NodeFactory.createLiteralNode( spelling, language, datatype );
		if (datatype.length() > 0) pl.present( datatype );
		String lf = FmtUtils.stringForNode( n, RDFUtils.noPrefixes ); 
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