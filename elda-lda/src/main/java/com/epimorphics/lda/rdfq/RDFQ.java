/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.vocabulary.RDF;


/**
    A skinny set of classes for representing SPARQL atomic terms,
    triples, and infix expressions.
    
 	@author chris
*/
public class RDFQ
	{
	public static final URINode RDF_TYPE = uri( RDF.type.getURI() );
	
	public static class Triple 
		{
		public final Any S, P, O;
		
		public Triple( Any S, Any P, Any O )
			{ this.S = S; this.P = P; this.O = O; }
		
		@Override public String toString()
			{ return asSparqlTriple( PrefixLogger.some() ); }
		
		public String asSparqlTriple( PrefixLogger pl )
			{ 
			return S.asSparqlTerm( pl ) + " " + P.asSparqlTerm( pl ) + " " + O.asSparqlTerm( pl ); 
			}
		}
	
	public static Value literal( double d )
		{
		String spelling = Double.toString( d );
		return new Value( spelling, "", "" ) 
			{
			@Override public String asSparqlTerm( PrefixLogger pl ) { return spelling; }
			};
		}
	
	public static Apply apply( String f, RenderExpression X ) 
		{ return new Apply( f, X ); }
	
	public static Infix infix( RenderExpression L, String op, RenderExpression R ) 
		{ return new Infix( L, op, R ); }
	
	public static URINode uri( String URI ) 
		{ return new URINode( URI ); }
	
	public static Value literal( String spelling ) 
		{ return new Value( spelling ); }
	
	public static Value literal( String spelling, String language, String datatype ) 
		{ return new Value( spelling, language, datatype ); }
	
	public static Variable var( String name ) 
		{ return new Variable( name ); }
	
	public static Triple triple( Any S, Any P, Any O ) 
		{ return new Triple( S, P, O ); }

	}