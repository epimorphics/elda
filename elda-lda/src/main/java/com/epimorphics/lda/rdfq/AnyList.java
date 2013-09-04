package com.epimorphics.lda.rdfq;

import java.util.Arrays;
import java.util.List;

import com.epimorphics.lda.support.PrefixLogger;

/**
 	An AnyList is an RDFQ value representing a SPARQL bracketed list
 	construct. 
*/
public class AnyList extends Any {

	protected final List<Any> elements;
	
	/**
	    Initialise this AnyList with a list of elements. The passed array
	    should never be altered once this has happened.
	*/
	public AnyList( Any... elements ) {
		this.elements = Arrays.asList( elements );
	}
	
	/**
	    Return the list of elements in this AnyList. The list must not
	    be modified.
	*/
	public List<Any> getElements() {
		return elements;
	}

	public int size() {
		return elements.size();
	}
	
	@Override public String toString() {
		return asSparqlTerm( new PrefixLogger() );
	}
	
	@Override public boolean equals( Object other ) {
		return other instanceof AnyList && same( (AnyList) other );
	}
	
	private boolean same(AnyList other) {
		return elements.equals( other.elements );
	}

	@Override public int hashCode() {
		return elements.hashCode();
	}

	/**
	    Render this AnyList as a SPARQL expression: an open bracket,
	    space-separated SPARQL terms for the elements, a close bracket.
	*/
	@Override public String asSparqlTerm(PrefixLogger pl) {
		StringBuilder sb = new StringBuilder();
		sb.append( "(" );
		for (Any e: elements) sb.append( " " ).append( e.asSparqlTerm( pl ) );
		sb.append( ")" );
		return sb.toString();
	}
}
