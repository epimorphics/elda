package com.epimorphics.lda.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
    Prefix mapping
*/
public class PrefixLogger {

	public static final PrefixLogger Empty = new PrefixLogger( PrefixMapping.Extended );

	protected final PrefixMapping pm;
	
	protected final Set<String> seen = new HashSet<String>();
	
	public PrefixLogger( PrefixMapping pm ) {
		this.pm = pm;
	}
	
	/**
	    Present a URI as a SPARQL term, either <>-quoted, or
	    as a qname if there's a suitable prefix mapping for it.
	*/
	public String present( String URI ) {
		String qName = pm.qnameFor( URI );
		if (qName == null) return "<" + URI + ">";
		seen.add( qName.substring( 0, qName.indexOf( ':' ) ) );
		return qName;
	}
	
	/**
	    Write out the used prefixes as SPARQL prefix declarations.
	*/
	public StringBuilder writePrefixes( StringBuilder out ) {
		List<String> prefixes = new ArrayList<String>( seen );
		Collections.sort( prefixes );
		for (String prefix: prefixes) {
			out
			.append( "PREFIX " )
			.append( prefix )
			.append( ": <" )
			.append( pm.getNsPrefixURI(prefix).trim() ) 
			.append( ">\n" );
		}
		return out;
	}
	
}
