/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.rdfq;


/**
    A skinny set of classes for representing SPARQL atomic terms,
    triples, and infix expressions.
    
 	@author chris
*/
public class RDFQ
	{
	public static class Triple 
		{
		public final Any S, P, O;
		
		public Triple( Any S, Any P, Any O ) 
			{ this.S = S; this.P = P; this.O = O; }
		
		@Override public String toString()
			{ return S.asSparqlTerm() + " " + P.asSparqlTerm() + " " + O.asSparqlTerm(); }
		}
	
	public static LiteralNode literal( double d )
		{
		String spelling = Double.toString( d );
		return new LiteralNode( spelling, "", "" ) 
			{
			@Override public String asSparqlTerm() { return spelling; }
			};
		}
	
	public static Apply apply( String f, RenderExpression X ) 
		{ return new Apply( f, X ); }
	
	public static Infix infix( RenderExpression L, String op, RenderExpression R ) 
		{ return new Infix( L, op, R ); }
	
	public static URINode uri( String URI ) 
		{ return new URINode( URI ); }
	
	public static LiteralNode literal( String spelling ) 
		{ return new LiteralNode( spelling ); }
	
	public static LiteralNode literal( String spelling, String language, String datatype ) 
		{ return new LiteralNode( spelling, language, datatype ); }
	
	public static Variable var( String name ) 
		{ return new Variable( name ); }
	
	public static Triple triple( Any S, Any P, Any O ) 
		{ return new Triple( S, P, O ); }
	}