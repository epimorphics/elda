/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.epimorphics.util.RDFUtils;

/**
    Class to edit models by editing all the resource URIs according to the 
    rules for a PrefixEditor.
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
	
	public boolean isEmpty() {
		return pe.isEmpty();
	}
	
	/**
	    Rename the keys of the term-binding map according to this prefix editor.
	*/
	public Map<String, String> rename( Map<String, String> tb ) {
		if (pe.isEmpty()) return tb;
		Map<String, String> renamed = new HashMap<String, String>(tb.size());
		for (Map.Entry<String, String> e: tb.entrySet()) {
			renamed.put( pe.rename(e.getKey()),  e.getValue());
		}
		return renamed;
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
		Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
		Node newS = rename(S), newP = rename(P), newO = rename(O);
		if (newS == S && newP == P && newO == O) return t;
		return Triple.create(newS, newP, newO);
	}

	private Node rename(Node o) {
		if (o.isBlank()) return o;
		if (o.isLiteral()) return rename( (Node_Literal) o );
		String uri = o.getURI(), newUri = pe.rename(uri);
		if (newUri == uri) return o;
		return NodeFactory.createURI( newUri );
	}
	
	private Node rename( Node_Literal o ) {
		String typeURI = o.getLiteralDatatypeURI();
		if (typeURI == null) return o;
		String newURI = pe.rename( typeURI );
		if (newURI == typeURI) return o;
		return NodeFactory.createLiteral( o.getLiteralLexicalForm(), typeNamed( newURI ) );
	}

	public RDFNode rename( RDFNode n ) {
		if (n.isAnon()) return n;
		if (n.isLiteral()) return rename( (Literal) n );
		Resource r = n.asResource();
		String givenURI = r.getURI();
		String uri = pe.rename( givenURI );
		return uri == givenURI ? n : r.getModel().createResource( uri );
	}
	
	private Literal rename( Literal n ) {
		String typeURI = n.getDatatypeURI();
		if (RDFUtils.isBareStringType(typeURI)) return n;
		String newURI = pe.rename( typeURI );
		if (typeURI.equals(newURI)) return n;
		return ResourceFactory.createTypedLiteral( n.getLexicalForm(), typeNamed(newURI) );
	}
	
	private RDFDatatype typeNamed(String typeURI) {
		return TypeMapper.getInstance().getSafeTypeByName( typeURI );
	}
}