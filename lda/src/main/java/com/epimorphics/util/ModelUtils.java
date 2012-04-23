package com.epimorphics.util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
		if (!S.isBlank()) result = S.hashCode() << 32;
		if (!P.isBlank()) result ^= P.hashCode() << 16;
		if (!O.isBlank()) result ^= O.hashCode();
		return result;
	}

}
