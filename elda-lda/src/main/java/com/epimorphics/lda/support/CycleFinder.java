package com.epimorphics.lda.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 	<p>
	Find the cyclic nodes in a graph, using Tarjen's algorithm
	for finding strongly connected components: see
	
	<a href="http://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">
	the Wikipedia entry</a>.
	</p>
	
	<p>
	The model underlying the resources to be searched for cyclicity is
	converted to a simpler form that collapses all the properties linking
	A to B into a single edge and makes the index fields of a node directly
	accessible.
	</p>
*/
public class CycleFinder {

	int index = 0;
	
	Stack<Node> S = new Stack<Node>();
	
	Set<Resource> cyclics = new HashSet<Resource>();

	Map<Resource, Node> nodes = new HashMap<Resource, Node>();
	
	CycleFinder(Model m) {
		convert(m);
	}
	
	public static Set<Resource> findCycles( Resource x ) {
		return findCyclics(x.getModel());
	}
	
	public static Set<Resource> findCyclics(Model m) {
		return new CycleFinder(m).find_cyclics(m);
	}
	
	Set<Resource> find_cyclics(Model m) {
		for (Map.Entry<Resource, Node> e: nodes.entrySet()) {
			stronglyConnect(e.getValue());
		}
		return cyclics;
	}
	
	public static class Node {
		
		final Resource r;
		int index;
		int lowlink;
		
		Set<Node> others = new HashSet<Node>();
		
		Node(Resource r) {
			this.r = r;
			this.index = -1;
			this.lowlink = -1;
		}
		
		void addEdgeTo(Node other) {
			others.add(other);
		}
		
		boolean isSelfCyclic() {
			return others.contains(this);
		}
	}
	
	/**
	    Convert a model to a set of nodes with edges to other nodes
	*/
	void convert(Model m) {
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			Statement s = it.next();
			RDFNode o = s.getObject();
			if (o.isResource()) convert(s.getSubject(), o.asResource());
		}
	}
	
	void convert(Resource s, Resource o) {
		convert(s).addEdgeTo(convert(o));
	}
	
	Node convert(Resource x) {
		Node n = nodes.get(x);
		if (n == null) nodes.put(x, n = new Node(x));
		return n;
	}
	
	boolean isSelfCyclic(Resource x) {
		return convert(x).isSelfCyclic();
	}
	
	/**
	    The Tarjan algorithm.
	*/
	void stronglyConnect(Node v) {
		v.index = index;
		v.lowlink = index;
		index += 1;
		S.push(v);	
		
		for (Node w: v.others) {
			if (w.index < 0) {
				stronglyConnect(w);
				v.lowlink = Math.min(v.lowlink, w.lowlink);
			} else if (S.contains(w)) {
				v.lowlink = Math.min(v.lowlink, w.index);
			}
		}
		
		if (v.lowlink == v.index) {
			CycleFinder.Component c = new Component();
			while (true) {
				Node w = S.pop();
				c.add( w.r );
				if (v == w) break;
			}
			hereIsAComponent(c);
		}
	}
	
	/**
	    Cyclic nodes are those in a nonsingular strongly connected
	    component or those that have an edge back to themselves.
	*/
	void hereIsAComponent(CycleFinder.Component c) {
		if (c.size() > 1 || isSelfCyclic(c.iterator().next())) {
			cyclics.addAll(c);
		}
	}

	/**
	    Just to abbreviate `HashSet<Resource>`.
	*/
	static class Component extends HashSet<Resource> {
		
		private static final long serialVersionUID = 1L;
	}		

}