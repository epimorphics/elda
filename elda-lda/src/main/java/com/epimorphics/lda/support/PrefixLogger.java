package com.epimorphics.lda.support;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.epimorphics.lda.rdfq.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.util.FmtUtils;

/**
    Prefix mapping
*/
public class PrefixLogger {

	protected final PrefixMapping pm;
	
	protected final Set<String> seen = new HashSet<String>();
	
	public PrefixLogger( PrefixMapping pm ) {
		this.pm = pm;
	}
	
	public PrefixLogger() {
		this( PrefixMapping.Factory.create() );
	}
	
	/**
	    <p>Present a URI as a SPARQL term, either <>-quoted, or
	    as a qname if there's a suitable prefix mapping for it.
	    </p>
	    
	    <p>SPARQL qname local names can't end with ".", so we
	    protect against generating illegal SPARQL by an ad-hoc
	    check against the URL ending with dot. May need to
	    consider other characters too, and may want to push
	    this protection into Jena. 
	    </p>
	*/
	public String present( String unsafeURI ) {
		String URI = FmtUtils.stringEsc(unsafeURI);
		if (URI.endsWith( ".") ) return "<" + URI + ">";
		String qName = pm.qnameFor( URI );
		if (qName == null) return "<" + URI + ">";
		seen.add( qName.substring( 0, qName.indexOf( ':' ) ) );
		return qName;
	}

	public String present( Any r ) {
		if (r instanceof Variable) return ((Variable) r).asSparqlTerm(this);
		return present( ((URINode) r).spelling() );
	}
	
	/**
	    Write out the used prefixes as SPARQL prefix declarations
	    into the StringBuilder <code>out</code>. Answer that same
	    StringBuilder.
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
	 	fragment and add them to <code>seen</code>.
	*/
	public void findPrefixesIn( String fragment ) {
		Matcher m = candidatePrefix.matcher( fragment );
		while (m.find()) {
			String candidate = m.group(1);
			if (pm.getNsPrefixURI( candidate ) != null) seen.add( candidate );
		}
	}
	
	/*
	    Taken from http://www.w3.org/TR/rdf-sparql-query/#rPN_CHARS_BASE
	    and modified to suit Java regexps. Note the exclusion of the
	    [#x10000-#xEFFFF] range because that's outside the 16-bit range.
	*/

	private static final String PN_CHARS_BASE = 
		"A-Z a-z"
		+ " \u00C0-\u00D6 \u00D8-\u00F6 \u00F8-\u02FF"
		+ " \u0370-\u037D \u037F-\u1FFF \u200C-\u200D \u2070-\u218F"
		+ " \u2C00-\u2FEF \u3001-\uD7FF \uF900-\uFDCF \uFDF0-\uFFFD"
		;
	                                                                                                                                                                                                                                                                   
	private static final String PN_CHARS_U = PN_CHARS_BASE + "_";
	
	private static final String PN_CHARS = PN_CHARS_U + " 0-9 - \u00b7 \u0300-\u036f \u203f-\u2040";
	
	private static final String PN_PREFIX = "[" + PN_CHARS_BASE + "]([" + PN_CHARS + ".]*" + "[" + PN_CHARS + "])?";
	
	/**
	    A pattern that will match candidate prefixes, which will then be checked
	    against the provided prefix mapping. Note that it does not matter if the
	    pattern is over-generous in matching so long as it does not <i>miss</i>
	    any prefixes. Hence, it is not necessary to check for comments or strings.
	*/
	 public static final Pattern candidatePrefix = Pattern.compile( "(" + PN_PREFIX.replaceAll( " ", "" ) + "):" ); 
//	public static final Pattern candidatePrefix = Pattern.compile( "([A-Za-z][-+.A-Z_a-z0-9]*):" ); // ( "([^0-9<>:\\s\\.-][^:<>\\s]+):" );

	/**
	    Answer a new PrefixLoggger with a few standard prefixes in it.
	*/
	public static PrefixLogger some() {
		return new PrefixLogger( PrefixMapping.Extended );
	}
	
	/**
	 	PrefixMapping is required by extension queries
	*/
	public PrefixMapping getPrefixMapping() {
		return pm;
	}
	
}
