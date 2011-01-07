package com.epimorphics.lda.rdfq;

public abstract class Term extends Any 
	{
	public abstract Term replaceBy( String r );
	
	public abstract String spelling();
	
	public StringBuilder renderWrapped( StringBuilder out )
		{ return out.append( asSparqlTerm() ); }
	}