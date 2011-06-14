package com.epimorphics.lda.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
    Prefix mapping
*/
public class PrefixLogger {

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

	/**
	 	Look for plausible candidates for prefixes in the SPARQL
	 	fragment an add them to <code>seen</code>.
	*/
	public void findPrefixesIn( String fragment ) {
		Matcher m = candidatePrefix.matcher( fragment );
		while (m.find()) {
			String candidate = m.group(1);
			if (pm.getNsPrefixURI( candidate ) != null) seen.add( candidate );
		}
	}

	/**
	    A pattern that will match candidate prefixes, which will then be checked
	    against the provided prefix mapping. Note that it does not matter if the
	    pattern is over-generous in matching so long as it does not <i>miss</i>
	    any prefixes. Hence, it is not necessary to check for comments or strings.
	*/
	public static final Pattern candidatePrefix = Pattern.compile( "([A-Za-z_-]+):" );
	
	/**
	    Answer a new PrefixLoggger with a few standard prefixes in it.
	*/
	public static PrefixLogger some() {
		return new PrefixLogger( PrefixMapping.Extended );
	}
	
}
