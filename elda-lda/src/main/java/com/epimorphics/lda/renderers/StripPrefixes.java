package com.epimorphics.lda.renderers;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.vocabs.NsUtils;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.WrappedGraph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    StripPrefixes provides a method for removing unnecessary prefixes from
    (a (shared) copy of) a model.
*/
public class StripPrefixes {

	/**
	    Do returns a Model with the same contents as <code>a</code>
	    but using only prefixes that have namespaces which are used inside
	    <code>a</code>. The actual triples are shared, so while construction
	    requires a complete scan of the triples of <code>a</code>, it does not
	    use (much) additional space.
	*/
	public static Model Do(Model a) {
		final Set<String> namespaces = new HashSet<String>();
	//
		ExtendedIterator<Triple> triples = a.getGraph().find( Triple.ANY );
		while (triples.hasNext()) {
			Triple t = triples.next();
			StripPrefixes.addNamespace( namespaces, t.getSubject() );
			StripPrefixes.addNamespace( namespaces, t.getPredicate() );
			StripPrefixes.addNamespace( namespaces, t.getObject() );
		}
	//
		final PrefixMapping pm = PrefixMapping.Factory.create();
		for (String ns: namespaces) {
			String prefix = a.getNsURIPrefix( ns );
			if (prefix != null) pm.setNsPrefix(prefix, ns);
		}
	//
		Graph gg = new WrappedGraph( a.getGraph() ) {
			
			@Override public PrefixMapping getPrefixMapping() {
				return pm;
			}
		};
	//
		return ModelFactory.createModelForGraph( gg );
	}

	public static void addNamespace(Set<String> namespaces, Node n) {
		if (n.isURI()) 
			namespaces.add(n.getNameSpace());
		if (n.isLiteral()) {
			String uri = n.getLiteralDatatypeURI();
			if (uri != null) namespaces.add( NsUtils.getNameSpace( uri ) );
		}
	}

}
