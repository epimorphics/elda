/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.rdfq;

import com.epimorphics.lda.support.PrefixLogger;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;


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
		
		@Override public boolean equals( Object other ) {
			return other instanceof Triple && same( (Triple) other );
		}
		
		private boolean same(Triple other) {
			return S.equals(other.S) && P.equals(other.P)&& O.equals(other.O);
		}
		
		@Override public int hashCode() {
			return S.hashCode() + (P.hashCode() << 8) + (O.hashCode() << 16);
		}

		public String asSparqlTriple( PrefixLogger pl )
			{ 
			return S.asSparqlTerm( pl ) + " " + P.asSparqlTerm( pl ) + " " + O.asSparqlTerm( pl ); 
			}
		}
	
	static final String typeInteger = XSD.xint.getURI();

	static final String typeDouble = XSD.xdouble.getURI();

	public static Value literal( double d )
		{
		String spelling = Double.toString( d );
		return new Value( spelling, "",typeDouble ) 
			{
			@Override public String asSparqlTerm( PrefixLogger pl ) { return spelling; }
			};
		}
	
	public static Value literal( int i )
		{
		String spelling = Integer.toString( i );
		return new Value( spelling, "", typeInteger ) 
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

	public static AnyList list( Any... elements ) {
		return new AnyList( elements );
	}

	public static Any any(RDFNode rdf) {
		Node n = rdf.asNode();
		if (n.isURI()) return uri( n.getURI());
		if (n.isLiteral()) return literal( n.getLiteralLexicalForm(), n.getLiteralLanguage(), n.getLiteralDatatypeURI() );
		throw new RuntimeException( "Cannot convert " + rdf + " to RDFQ.Any" );
	}

	}