package com.epimorphics.lda.support;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
    Class to edit models by editing all the resource subject and
    object (not predicate) URIs according to the rules for a 
    PrefixEditor.
*/
public class ModelPrefixEditor {

	protected final PrefixEditor pe = new PrefixEditor();
	
	public ModelPrefixEditor() {	
	}

	public ModelPrefixEditor set( String from, String to ) {
		pe.set( from, to );
		return this;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof ModelPrefixEditor && same( (ModelPrefixEditor) other );
	}
	
	@Override public int hashCode() {
		return pe.hashCode();
	}
	
	@Override public String toString() {
		return "<Model_" + pe.toString() + ">";
	}
	
	private boolean same(ModelPrefixEditor other) {
		return pe.equals(other.pe);
	}

	public Model rename( Model x ) {
		if (pe.isEmpty()) return x;
		Model result = ModelFactory.createDefaultModel();
		Graph from = x.getGraph(), to = result.getGraph();
		rename( from, to );
		return result;
	}

	private void rename( Graph from, Graph to ) {
		if (!pe.isEmpty()) {
			ExtendedIterator<Triple> triples = from.find( Node.ANY, Node.ANY, Node.ANY );
			while (triples.hasNext()) to.add( rename( triples.next() ) );
		}
	}

	public Graph rename( Graph from ) {
		if (pe.isEmpty()) return from;
		Graph to = ModelFactory.createDefaultModel().getGraph();
		rename( from, to );
		return to;
	}

	private Triple rename(Triple t) {
		Node S = t.getSubject(), O = t.getObject();
		Node newS = rename(S), newO = rename(O);
		if (newS == S && newO == O) return t;
		return Triple.create(newS, t.getPredicate(), newO);
	}

	private Node rename(Node o) {
		if (o.isBlank() || o.isLiteral()) return o;
		String uri = o.getURI(), newUri = pe.rename(uri);
		if (newUri == uri) return o;
		return Node.createURI( newUri );
	}
	
}