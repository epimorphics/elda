package com.epimorphics.lda.support;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
    CycleFinder finds the resources in a model that are involved in
    cycles when the model is entered using specified resource.
    (Any parts of the model that are not reachable from that
    resource are not considered. "Involvement" does not count
    being used as a predicate.)
*/
public class CycleFinder {

	CycleFinder.Cons trace = null;
	
	Set<Resource> seen = new HashSet<Resource>();
	Set<Resource> cyclic = new HashSet<Resource>();
	
	/**
	    Answer all the resources in the model that x is in
	    that are accessible from x and are involved in cycles.
	*/
	public static Set<Resource> findCycles( Resource x ) {
		CycleFinder cf = new CycleFinder();
		cf.crawl( x );
		return cf.cyclic;
	}
	
	public void crawl( Resource x ) {
		if (seen( x )) {
			markCyclic( x );
		} else {
			add( x );
			for (StmtIterator sit = x.listProperties(); sit.hasNext();) {
				RDFNode n = sit.next().getObject();
				if (n.isResource()) crawl( n.asResource() );
			}
			remove( x );
		}
	}
	
	public boolean seen( Resource x ) {
		return seen.contains( x );
	}

	public void remove(Resource x) {
		seen.remove( x );
		trace = trace.tail;
	}

	public void add( Resource x ) {
		seen.add( x );
		trace = new Cons( x, trace );
	}

	public void markCyclic(Resource x) {
		CycleFinder.Cons t = trace;
		while (true) {
			cyclic.add( t.head );
			if (t.head.equals(x)) break;
			t = t.tail;
		}
		cyclic.add( x );
	}

	static class Cons {
		Resource head;
		CycleFinder.Cons tail;
		
		Cons(Resource head, CycleFinder.Cons tail) {
			this.head = head; this.tail = tail;
		}
	}
	
}