/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.util.FmtUtils;

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
		String lang = (language.equals("none") ? "" : language);
		RDFDatatype dt = datatype.length() == 0 ? null : TypeMapper.getInstance().getSafeTypeByName(datatype);
		Node n = NodeFactory.createLiteral( spelling, lang, dt );
		if (datatype.length() > 0) pl.present( datatype );
		String lf = FmtUtils.stringForNode( n, RDFUtils.noPrefixes ); 
		return lf;
		}
	
	/**
	    Answer a new Value with the same language and datatype as this
	    one, but with a new lexical form aka valueString vs.
	 */
	@Override public Value replaceBy( String vs ) 
		{ return new Value( vs, language, datatype ); }
	
	@Override public String spelling() 
		 { return spelling; }
	
	public String lang() 
	 	{ return language; }
	
	public String type()
		{ return datatype; }
	
	@Override public boolean equals( Object other )
		{ return other instanceof Value && same( (Value) other ); }
	
	@Override public int hashCode() {
		return spelling.hashCode() + language.hashCode() + datatype.hashCode();
	}
	
	private boolean same( Value other ) 
		{ 
		return 
			spelling.equals( other.spelling ) 
			&& language.equals( other.language ) 
			&& datatype.equals( other.datatype )
			; 
		}
	}