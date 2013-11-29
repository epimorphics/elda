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
public class CycleFinder_Tarjan {
		
	int index = 0;
	
	Stack<Node> S = new Stack<Node>();
	
	Set<Resource> cyclics = new HashSet<Resource>();

	Map<Resource, Node> nodes = new HashMap<Resource, Node>();
	
	CycleFinder_Tarjan() {
	}
	
	static class Node {
		Resource r;
		int index;
		int lowlink;
		
		Set<Node> others = new HashSet<Node>();
		
		Node(Resource r) {
			this.r = r;
			this.index = -1;
			this.lowlink = -1;
		}
		
		void add(Node other) {
			others.add(other);
		}
		
		boolean isSelfCyclic() {
			return others.contains(this);
		}
	}
	
	
	void convert(Model m) {
		for (StmtIterator it = m.listStatements(); it.hasNext();) {
			Statement s = it.next();
			RDFNode o = s.getObject();
			if (o.isResource()) convert(s.getSubject(), o.asResource());
		}
	}
	
	void convert(Resource s, Resource o) {
		Node sn = convert(s), on = convert(o);
		sn.add(on);
	}
	
	Node convert(Resource x) {
		Node n = nodes.get(x);
		if (n == null) nodes.put(x, n = new Node(x));
		return n;
	}
	
	Set<Resource> doFindCyclics(Set<Resource> roots) {
		if (roots.isEmpty()) return new HashSet<Resource>();
		return doFindCyclics( roots.iterator().next().getModel() );
	}
	
	Set<Resource> doFindCyclics(Model m) {
		convert( m );		
		for (Map.Entry<Resource, Node> e: nodes.entrySet()) {
			stronglyConnect(e.getValue());
		}
		return cyclics;
	}

	public static Set<Resource> findCyclics(Model m) {
		CycleFinder_Tarjan s = new CycleFinder_Tarjan();
		return s.doFindCyclics(m);
	}
	
	public static Set<Resource> findCyclics(Set<Resource> roots) {
		CycleFinder_Tarjan s = new CycleFinder_Tarjan();
		return s.doFindCyclics(roots);
	}
	
	boolean isSelfCyclic(Resource x) {
		return convert(x).isSelfCyclic();
	}
	
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
			CycleFinder_Tarjan.Component c = new Component();
			while (true) {
				Node w = S.pop();
				c.add( w.r );
				if (v == w) break;
			}
			hereIsAComponent(c);
		}
	}
	
	void hereIsAComponent(CycleFinder_Tarjan.Component c) {
		if (c.size() > 1 || isSelfCyclic(c.iterator().next())) {
			cyclics.addAll(c);
		}
	}
	
	static class Component extends HashSet<Resource> {
		
	}		

}