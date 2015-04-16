/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.specs;

import java.util.*;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
	PropertyExpiryTimes is a configurable map from resources (represented
	by Nodes) to their cache expiry time (interval length) in seconds.
	A PropertyExpiryTimes is configured from a model containing
	EXTRAS.cacheExpiryTime statements. A PropertyExpiryTimes also records
	the minimum of all specified cache expiry times it has been given.
*/
public class PropertyExpiryTimes {

	protected final Map<Node, Long> secondsForNode = new HashMap<Node, Long>();
	
	protected long minTimeSeconds = Long.MAX_VALUE / 1000;
	
	private PropertyExpiryTimes() {
	}
	
	/**
	    buildForTests constructs a PropertyExpiryTimes from an array of
	    alternating Node and (seconds) Long values; the node in element
	    N of the array is given the seconds value in element N+1.
	*/
	public static PropertyExpiryTimes buildForTests(Object ... args) {
		PropertyExpiryTimes result = new PropertyExpiryTimes();
		for (int i = 0; i < args.length; i += 2)
			result.put( ((Resource) args[i]).asNode(), (Long) args[i+1] );
		return result;
	}

	/**
	    assemble builds a PropertyExpiryTimes from a given model by looking
	    at all the statements (S EXTRAS.cacheExpiryTime O) where S is
	    a property, that is, has rdf:type rdf:Property, owl:DatatypeProperty,
	    or owl:ObjectProperty. S is added to the PropertyExpiryTimes with
	    a millisecond value derived from O, which must either be an
	    integer number of seconds or a string d+S where d+ is an integer
	    representation and S specifies the time unit for this time:
	    s(econds), m(inutes), h(ours), d(ays), w(eeks).
	*/
	public static PropertyExpiryTimes assemble(Model model) {
		PropertyExpiryTimes result = new PropertyExpiryTimes();
		List<Statement> candidates = model
			.listStatements(null, ELDA_API.cacheExpiryTime, (RDFNode) null)
			.toList()
			;
		for (Statement c: candidates)
			if (isProperty( c.getSubject())) {
				long seconds = getSecondsValue(c.getSubject(), ELDA_API.cacheExpiryTime, -1);
				result.put(c.getSubject().asNode(), seconds);
			}
				
		return result;
	}
	
	protected void put(Node n, long seconds) {
		secondsForNode.put(n, seconds);
		if (seconds < minTimeSeconds) minTimeSeconds = seconds;
	}
	
	private static boolean isProperty(Resource s) {
		return 
			s.hasProperty(RDF.type, RDF.Property)
			|| s.hasProperty(RDF.type, OWL.DatatypeProperty)
			|| s.hasProperty(RDF.type, OWL.ObjectProperty)
			;
	}

	@Override public String toString() {
		return "<PET " + secondsForNode + ">";
	}
	
	@Override public boolean equals( Object other ) {
		return 
			other instanceof PropertyExpiryTimes 
			&& same( (PropertyExpiryTimes) other )
			;
	}

	private boolean same(PropertyExpiryTimes other) {
		return secondsForNode.equals(other.secondsForNode);
	}

	public static long getSecondsValue(Resource x, Property p, long ifAbsent) {
		Statement s = x.getProperty( p );
		if (s == null) return ifAbsent;
		RDFNode n = s.getObject();
		if (n.isResource()) return ifAbsent;
		if (n.asLiteral().getDatatypeURI() == null) {
			String spelling = n.asLiteral().getLexicalForm();
			char last = spelling.charAt(spelling.length() - 1);
			if (Character.isDigit(last)) {
				return Long.parseLong(spelling);
			} else {
				long l = Long.parseLong(spelling.substring(0, spelling.length() - 1));
				return l * scale(last);
			}
		} else {
			return n.asLiteral().getLong();
		}
	}
    
    static long scale(char last) {
		if (last == 's') return 1;
		if (last == 'm') return 60;
		if (last == 'h') return 60 * 60;
		if (last == 'd') return 60 * 60 * 24;
		if (last == 'w') return 60 * 60 * 24 * 7;
		return 1;
	}
    
    public long timeInMillisFor(Resource r) {
    	Long t = secondsForNode.get(r.asNode());
    	return t == null ? Long.MAX_VALUE : t * 1000;
    }
    
    public long minTimeMillis() {
    	return minTimeSeconds * 1000;
    }
}
