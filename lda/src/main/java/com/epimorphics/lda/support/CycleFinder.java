package com.epimorphics.lda.support;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
    CycleFinder finds the resources in a model that are involved in
    cycles when the model is entered using specified resource.
    (Any parts of the model that are not reachable from that
    resource are not considered. "Involvement" does not count
    being used as a predicate.)
*/
public class CycleFinder {

	CycleFinder.Trace trace = null;
	Set<Resource> inTrace = new HashSet<Resource>();
	Set<Resource> cyclic = new HashSet<Resource>();
	
	/**
	    Answer all the resources in the model that x is in
	    that are accessible from x and are involved in cycles.
	*/
	public static Set<Resource> findCycles( Resource x ) {
//		System.err.println( ">> finding cycles rooted at " + x );
		CycleFinder cf = new CycleFinder();
		cf.crawl( x );
		return cf.cyclic;
	}

	/**
	    Answer all the resources in the model that items are in
	    which are accessible from x and are involved in cycles.
	*/
	public static Set<Resource> findCycles( Set<Resource> items ) {
//		System.err.println( ">> finding cycles rooted at any of " + items );
//		items.iterator().next().getModel().write( System.err, "TTL" );
//		System.err.println( ">> that was the model, that was." );
		CycleFinder cf = new CycleFinder();
		for (Resource item: items) cf.crawl( item.asResource() );
		return cf.cyclic;
	}
	
	int depth = 0;
	
	void indent() {
		for (int i = 0; i < depth; i += 1) System.err.print( "| " );
		System.err.print( ">> " );
	}
	
	public void crawl( Resource x ) {
		
		depth += 1;
		
		if (true) { // !cyclic.contains( x )) {
//			indent(); System.err.println( "considering " + x );
			if (inTrace.contains( x )) {
//				indent(); System.err.println( "a Palpable Hit; he and his ancestors are cyclic." );
				markCyclic( x );
			} else {
//				indent(); System.err.println( "processing descendants ..." );
				add( x );
				for (StmtIterator sit = x.listProperties(); sit.hasNext();) {
					Statement s = sit.next();
					RDFNode n = s.getObject();
					trace.property = s.getPredicate();
//					indent(); System.err.println( "considering " + trace.property + "'s value " + n );
					if (n.isResource()) crawl( n.asResource() );
				}
				remove( x );
			}
		}
		
		depth -= 1;
	}
	
	public boolean inTrail( Resource x ) {
		return inTrace.contains( x );
	}

	public void remove(Resource x) {
		trace = trace.tail;
		inTrace.remove( x );
	}

	public void add( Resource x ) {
		inTrace.add( x );
		trace = new Trace( x, trace );
	}

	public void markCyclic(Resource x) {
		CycleFinder.Trace t = trace;
//		System.err.println( ">> CYCLE: " + x );
		while (true) {
//			System.err.println( ">>        " + t.property.getLocalName() + " <= " + t.head );
			cyclic.add( t.head );
			if (t.head.equals(x)) break;
			t = t.tail;
		}
	}

	static class Trace {
		Resource head;
		Property property;
		CycleFinder.Trace tail;
		
		Trace(Resource head, CycleFinder.Trace tail) {
			this.head = head; this.tail = tail;
		}
	}
	
}