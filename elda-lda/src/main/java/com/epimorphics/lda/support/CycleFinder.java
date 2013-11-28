package com.epimorphics.lda.support;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.*;

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
	Set<Resource> crawled = new HashSet<Resource>();
	
	/**
	    Answer all the resources in the model that x is in
	    that are accessible from x and are involved in cycles.
	*/
	public static Set<Resource> findCycles( Resource x ) {
		CycleFinder cf = new CycleFinder();
		cf.crawl( x );
		
//		System.err.print( ">>" );
//		for (Resource c: cf.cyclic) System.err.print( " " + c.getLocalName() ); 
//		System.err.println();
		
		return cf.cyclic;
	}

	/**
	    Answer all the resources in the model that items are in
	    which are accessible from x and are involved in cycles.
	*/
	public static Set<Resource> findCycles( Set<Resource> items ) {
		CycleFinder cf = new CycleFinder();
		for (Resource item: items) cf.crawl( item.asResource() );
		
//		System.err.print( ">>" );
//		for (Resource c: cf.cyclic) System.err.println( " " + c.getLocalName() ); 
//		System.err.println();
		
		return cf.cyclic;
	}
	
	public void crawl( Resource x ) {
//		System.err.println( ">> crawling " + x.getLocalName() );
		if (inTrace.contains( x )) {
//			System.err.print( ">>  in trace: " );
			markCyclic( x ); // System.err.println();
		} else if (crawled.contains(x)) {
//			System.err.println( ">>  already crawled" );
		} else {
			startCrawling( x );
			for (StmtIterator it = x.listProperties(); it.hasNext();) {
				Statement s = it.next();
				RDFNode n = s.getObject();
				trace.property = s.getPredicate();
				if (n.isResource()) {
//					System.err.println( ">>  doing " + x.getLocalName() + "." + trace.property.getLocalName() + " = " + n.asResource().getLocalName() );
					crawl( n.asResource() );
				}
			}
			doneCrawling( x );
		}
//		System.err.println( ">> crawled " + x.getLocalName() );
	}
	
	public boolean inTrail( Resource x ) {
		return inTrace.contains( x );
	}
	
	public void startCrawling( Resource x ) {
		inTrace.add( x );
		trace = new Trace( x, trace );
	}

	public void doneCrawling(Resource x) {
		trace = trace.tail;
		inTrace.remove( x );
		crawled.add(x);
	}

	public void markCyclic(Resource x) {
		CycleFinder.Trace t = trace;
		while (true) {
//			System.err.print( " " + t.head.getLocalName() );
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