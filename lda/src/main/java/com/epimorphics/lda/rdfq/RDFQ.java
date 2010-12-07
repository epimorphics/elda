/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
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
	public static abstract class Any 
		{
		@Override public String toString()
			{ throw new UnsupportedOperationException(); }
		
		public abstract String asSparqlTerm();
		}
	
	public static abstract class Fixed extends RDFQ.Any 
		{}
	
	public static class Resource extends RDFQ.Fixed 
		{
		final String URI;
		
		public Resource( String URI ) 
			{ this.URI = URI; }
		
		public String asSparqlTerm()
			{ return "<" + URI + ">"; }
		}
	
	public static class Literal extends RDFQ.Fixed 
		{
		final String spelling;
		final String language;
		final String datatype;
		
		public Literal( String spelling ) 
			{ this( spelling, "", "" ); }
		
		public Literal( String spelling, String language, String datatype ) 
			{ 
			this.spelling = spelling; 
			this.language = language == null ? "" : language; 
			this.datatype = datatype == null ? "" : datatype; 
			}
		
		public String asSparqlTerm()
			{ 
			String lf = "\"" + spelling.replaceAll( "\\\\", "\\\\" ) + "\"";
			if (language.length() > 0) return lf + "@" + language;
			if (datatype.length() > 0) return lf + "^^<" + datatype + ">";
			return lf;
			}
		}
	
	public static class Variable extends RDFQ.Any 
		{
		final String name;
		
		public Variable( String name )
			{ this.name = name; }
		
		public String name()
			{ return name; }
		
		public String asSparqlTerm()
			{ return name; }
		}
	
	public static class Triple 
		{
		public final Any S, P, O;
		
		public Triple( Any S, Any P, Any O ) 
			{ this.S = S; this.P = P; this.O = O; }
		}
	
	public static class Infix
		{
		public final Any L, R;
		public final String op;
		
		public Infix( Any L, String op, Any R )
			{ this.L = L; this.op = op; this.R = R; }
		}
	
	public static Literal literal( double d )
		{
		String spelling = Double.toString( d );
		return new Literal( spelling, "", "" ) 
			{
			@Override public String asSparqlTerm() { return spelling; }
			};
		}
	
	public static Infix infix( Any L, String op, Any R ) 
		{ return new Infix( L, op, R ); }
	
	public static Resource uri( String URI ) 
		{ return new Resource( URI ); }
	
	public static Literal literal( String spelling ) 
		{ return new Literal( spelling ); }
	
	public static Literal literal( String spelling, String language, String datatype ) 
		{ return new Literal( spelling, language, datatype ); }
	
	public static Variable var( String name ) 
		{ return new Variable( name ); }
	
	public static Triple triple( Any S, Any P, Any O ) 
		{ return new Triple( S, P, O ); }
	}