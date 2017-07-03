/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ModelUtils {
	
    public static long hashModel( Model m ) {
    	long result = 0;
    	ExtendedIterator<Triple> it = m.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
		while (it.hasNext()) result ^= hashTriple( it.next() );
    	return result;
	}

	public static long hashTriple(Triple t) {
		long result = 0;
		Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
		if (!S.isBlank()) result = (long) S.hashCode() << 32;
		if (!P.isBlank()) result ^= (long) P.hashCode() << 16;
		if (!O.isBlank()) result ^= (long) O.hashCode();
		return result;
	}

}
