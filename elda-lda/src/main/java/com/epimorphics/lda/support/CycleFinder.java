package com.epimorphics.lda.support;

import java.util.*;

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
		
//		Set<Resource> them = new HashSet<Resource>(); 
//		them.add(x); 
//		Set<Resource> tc = stub.Tarjan(them);
//		
////		System.err.print( ">>" );
////		for (Resource c: cf.cyclic) System.err.print( " " + c.getLocalName() ); 
////		System.err.println();
//		
//		if (tc.equals(cf.cyclic)) {} else {
//			System.err.println( ">> DIFFERENT");
//			System.err.println( ">> for: " + x );
//			System.err.println( ">> tarjan: " + tc );
//			System.err.println( ">> homwgrown: " + cf.cyclic );
//		}
		
		return cf.cyclic;
	}

	/**
	    Answer all the resources in the model that items are in
	    which are accessible from x and are involved in cycles.
	*/
	public static Set<Resource> findCycles( Set<Resource> items ) {
		CycleFinder cf = new CycleFinder();
		for (Resource item: items) cf.crawl( item );
		
//		Set<Resource> tc = stub.Tarjan(items);
//		
//		if (tc.equals(cf.cyclic)) {} else {
//			System.err.println( ">> DIFFERENT");
//			System.err.println( ">> for: " + items );
//			System.err.println( ">> tarjan: " + tc );
//			System.err.println( ">> homwgrown: " + cf.cyclic );
//		}
		
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
	
	static class stub {
		
		int index = 0;
		
		Stack<Resource> S = new Stack<Resource>();
		
		Map<Resource, Integer> indexes = new HashMap<Resource, Integer>();
		
		Map<Resource, Integer> lowlinks = new HashMap<Resource, Integer>();
		
		Set<Resource> remaining = new HashSet<Resource>();
		
		Set<Resource> cyclics = new HashSet<Resource>();
		
		stub() {
			
		}
		
		Set<Resource> tarjan(Set<Resource> roots) {
			
			for (Resource r: roots) {
				if (indexOf(r) < 0) stronglyConnect( r );
			}
			
			while (remaining.size() > 0) {
				Resource r = remaining.iterator().next();
				remaining.remove(r);
				if (indexOf(r) < 0) stronglyConnect( r );
			}
			
			return cyclics;
		}
		
		int indexOf(Resource r) {
			Integer i = indexes.get(r);
			return i == null ? -1 : i;
		}
		
		void setIndex(Resource r, int index) {
			indexes.put(r, index);
		}
		
		void setLowLink(Resource r, int index) {
			lowlinks.put(r, index);
		}
		
		int lowlink(Resource r) {
			return lowlinks.get(r);
		}
		
		static Set<Resource> Tarjan(Set<Resource> roots) {
			stub s = new stub();
			return s.tarjan(roots);
		}
		
		void stronglyConnect(Resource r) {
			setIndex( r, index );
			setLowLink( r, index );
			index += 1;
			S.push(r);	
			
			for (RDFNode o: r.listProperties().mapWith(Statement.Util.getObject).toList()) {
				if (o.isResource()) {
					Resource or = o.asResource();
					if (indexOf(or) < 0) {
						stronglyConnect(or);
						setLowLink(r, Math.min(lowlink(r), lowlink(or)));
					} else if (S.contains(or)) {
						setLowLink(r, Math.min(lowlink(r), indexOf(or)));
					}
				}
			}
			
			if (lowlink(r) == indexOf(r)) {
				Component c = new Component();
				while (true) {
					Resource w = S.pop();
					c.add( w );
					if (r.equals(w)) break;
				}
				hereIsAComponent(c);
			}
		}
		
		void hereIsAComponent(Component c) {
			if (c.size() > 1) {
				cyclics.addAll(c);
			}
		}
		
		static class Component extends HashSet<Resource> {
			
		}		
		
//		algorithm tarjan is
//		  input: graph G = (V, E)
//		  output: set of strongly connected components (sets of vertices)
//
//		  index := 0
//		  S := empty
//		  for each v in V do
//		     if (v.index is undefined) then
//		      strongconnect(v)
//		    end if
//		  end for
//
//		  function strongconnect(v)
//		    // Set the depth index for v to the smallest unused index
//		    v.index := index
//		    v.lowlink := index
//		    index := index + 1
//		    S.push(v)
//
//		    // Consider successors of v
//		    for each (v, w) in E do
//		       if (w.index is undefined) then
//		         // Successor w has not yet been visited; recurse on it
//		        strongconnect(w)
//		        v.lowlink  := min(v.lowlink, w.lowlink)
//		      else if (w is in S) then
//		         // Successor w is in stack S and hence in the current SCC
//		         v.lowlink  := min(v.lowlink, w.index)
//		      end if
//		    end for
//
//		    // If v is a root node, pop the stack and generate an SCC
//		    if (v.lowlink = v.index) then
//		      start a new strongly connected component
//		      repeat
//		        w := S.pop()
//		        add w to current strongly connected component
//		      until (w = v)
//		      output the current strongly connected component
//		    end if
//		  end function
	}
	
	
}