/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.shortnames;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.NsUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Another class maintaining shortnames, this one avoids
    commitments made in Context. It is layered; the first stage is
    what binds shortnames from the LDA config model, and stage2
    handles the model to be rendered. (The difference is that the
    shortnames from the config file are for query, but the ones
    that come later are for rendering.) 
    
    @author chris
*/
public class NameMap {

	/** to make listStatements readable */
	private static final Resource ANY = null;
	
	/** mapping short names to sets of full names */
	private final MultiMap<String, String> map = new MultiMap<String, String>();
	
	/** combined prefix mapping from all sources */
	private final PrefixMapping prefixes = PrefixMapping.Factory.create();

	/** 
	 	load a given prefix mapping and model into the map. Looks in the
	 	model for anything with an rdfs:label, api:label, or api:name
	 	property.
	*/
	public void load( PrefixMapping pm, Model m ) {
		prefixes.withDefaultMappings( pm );
		load( pm, m.listStatements( ANY, RDFS.label, ANY ) );
		load( pm, m.listStatements( ANY, API.label, ANY ) );
		load( pm, m.listStatements( ANY, API.name, ANY ) );
	}

	private void load( PrefixMapping pm, StmtIterator si ) {
		while (si.hasNext()) load( pm, si.next() );
	}

	private void load(PrefixMapping pm, Statement s) {
		Resource r = s.getSubject();
		String name = asString( s.getObject() );
		map.add( r.getURI(), name );
	}
	
	/**
	    The string version of a node, being its URI if it's a
	    Resource, and its lexical form if it's a Literal.
	*/
	private String asString( RDFNode r ) {
		Node n = r.asNode();
		return n.isLiteral() ? n.getLiteralLexicalForm() : n.getURI();
	}
	
	/**
	    During Stage2, clashing shortnames are resolved rather than permitted.
	*/
	public Stage2NameMap stage2(boolean stripHas) {
		return new Stage2NameMap( stripHas, this );
	}

	/**
	    A Stage2 NameMap adds names to the map, but arranges that if 
	    several full names map to the same localname, then the short
	    forms are prefixed by the declared prefixes of their namespaces.
	    It scans the entire model for properties and datatypes so that
	    the mapping doesn't depend on the (random) order that different
	    full names are encountered in.
	*/
	public static class Stage2NameMap {
		
		/**
		    We need to ensure that the terms used by Elda will always
		    have prefixes available, so we build an automatic prefix
		    mapping which will be used as default.
		*/
		private static PrefixMapping automatic = PrefixMapping.Factory.create()
			.setNsPrefix( "rdf", RDF.getURI() )
			.setNsPrefix( "rdfs", RDFS.getURI() )
			.setNsPrefix( "xhv", XHV.getURI() )
			.setNsPrefix( "dct", DCTerms.getURI() )
			;

		/** The combined namespace prefixes from all models. */
		private PrefixMapping prefixes = PrefixMapping.Factory.create();
		
		/** The terms -- predicates and literal types -- of the models. */
		private Set<String> terms = new HashSet<String>();
		
		/** the mapping from full URIs to all their allowed shortnames.*/
		private MultiMap<String, String> uriToName = new MultiMap<String, String>();
		
		/** true if we have to convert "hasSpoo" to "spoo". */
		private boolean stripHas;
		
		/** Construct a Stage2 map from a NameMap. */
		public Stage2NameMap( boolean stripHas, NameMap nm ) {
			this.stripHas = stripHas;
			this.prefixes.setNsPrefixes( nm.prefixes );
			this.prefixes.setNsPrefixes( automatic );
			this.uriToName.addAll( nm.map );
		}

		/** Load a prefix mapping and the terms of a model */
		public Stage2NameMap load( PrefixMapping pm, Model m ) {
			prefixes.withDefaultMappings( pm );
			loadPredicatesOf( m );
			return this;
		}

		private void loadPredicatesOf( Model m ) {
			for (StmtIterator sit = m.listStatements(); sit.hasNext();) {
				Statement s = sit.next();
				terms.add( s.getPredicate().getURI() );
				Node o = s.getObject().asNode();
				if (o.isLiteral()) {
					String type = o.getLiteralDatatypeURI();
					if (type != null) terms.add( type );
				}
			}
		}

		/**
		    Answer a map from full URIs to sets of short names. The sets
		    will always be singletons. If a URI already has a short name,
		    that's what will be used. URIs that don't yet have one will
		    be given their local name if it's unambiguous, or their prefixed
		    local name if needed to disambiguate.
		    
		    TODO: deal with labels with bad syntax.
		*/
		public MultiMap<String, String> result() {
			Map<String, Set<String>> shorts = new HashMap<String, Set<String>>();
			Set<String> already = new HashSet<String>();
			for (String p: terms) {
				String givenShort = uriToName.getOne( p );
				if (givenShort == null) {
					String local = NsUtils.getLocalName(p);
					Set<String> ps = shorts.get(local);
					if (ps == null) shorts.put( local, ps = new HashSet<String>() );
					ps.add( p );
				} else {
					already.add( givenShort );
				}
			}
			for (String shortName: shorts.keySet()) {
				Set<String> ps = shorts.get(shortName);
				if (already.contains( shortName ) || ps.size() > 1) {
					for (String uri: ps) 
						uriToName.add( uri, prefixFor( NsUtils.getNameSpace(uri) ) + t(shortName) );
				} else {
					uriToName.add( ps.iterator().next(), t(shortName) );
				}
			}
			return uriToName;
		}

		// just a HACK to handle has-stripping
		private String t(String x) {
			if (stripHas && x.startsWith("has") && x.length() > 3) {
				char ch = x.charAt(3);
				if (Character.isUpperCase(ch))
					return Character.toLowerCase(ch) + x.substring(4);
			}
			return x;
		}

		/**
		    Answer a prefix for a namespace. If there's one in the
		    prefixes, use that. If there aren't any, use "none_".
		    If the namespace is magic, ie its prefix MUST NOT be used 
		    on pain of confusing certain renderers, omit it entirely and 
		    live with ambiguity.
		*/
		private String prefixFor( String nameSpace ) {
			if (NsUtils.isMagic( nameSpace )) return "";
			String prefix = prefixes.getNsURIPrefix( nameSpace );
			return prefix == null ? "none_" : prefix + "_";
		}
	}
}
