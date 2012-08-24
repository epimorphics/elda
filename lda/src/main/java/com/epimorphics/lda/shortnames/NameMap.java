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
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.NsUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
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
		// load( pm, m.listStatements( ANY, API.name, ANY ) );
	}
	
	/**
	    load vocabulary elements from m that are not already
	    defined in this NameMap.
	*/
	public void loadIfNotDefined( PrefixMapping pm, Model m ) {
		NameMap inner = new NameMap();
		inner.load( pm, m );
		for (String shortName: inner.map.keySet()) {
			if (!map.containsKey(shortName)) {
				map.add(shortName, inner.map.getAll( shortName ) );
			}
		}
	}

	private void load( PrefixMapping pm, StmtIterator si ) {
		while (si.hasNext()) load( pm, si.next() );
	}

	private void load(PrefixMapping pm, Statement s) {
		Resource S = s.getSubject();
		if (S.isURIResource()) map.add( S.getURI(), asString( s.getObject() ) );
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
		
		/** Terms which arrive only in the model and hence are subordinate
		 	to terms that have been define explicitly.
		 */
		private Set<String> modelTerms = new HashSet<String>();
		
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
		public Stage2NameMap loadPredicates( PrefixMapping pm, Model m ) {
			prefixes.withDefaultMappings( pm );
			loadPredicatesOf( m );
			return this;
		}

		private void loadPredicatesOf( Model m ) {
			for (StmtIterator sit = m.listStatements(); sit.hasNext();) {
				Statement s = sit.next();
				modelTerms.add( s.getPredicate().getURI() );
				Node o = s.getObject().asNode();
				if (o.isLiteral()) {
					String type = o.getLiteralDatatypeURI();
					if (type != null) modelTerms.add( type );
				}
			}
		}

		/**
		    Answer a map from full URIs to the corresponding short names.
		    If a URI already has a short name, that's what will be used. 
		    URIs that don't yet have one will be given their local name if 
		    it's unambiguous, or their prefixed local name if needed to 
		    disambiguate.
		    
		    TODO: deal with labels with bad syntax.
		*/
		public Map<String, String> result() {
			Map<String, Set<String>> shortnameToURIs = new HashMap<String, Set<String>>();
			Map<String, Set<String>> predefinedShortNames = new HashMap<String, Set<String>>();
			for (String uri: uriToName.keySet()) {
				addAnother( shortnameToURIs, uri, uriToName.getAll(uri) );
				addPredefined( uriToName, predefinedShortNames, uri );
			}
			
//			for (String p: terms) {
//				String givenShort = uriToName.getOne( p );
//				if (givenShort == null) addURIforShortname( shortnameToURIs, NsUtils.getLocalName(p), p );
//			}
		//
			Map<String, String> result = new HashMap<String, String>();
			for (String shortName: shortnameToURIs.keySet()) {
				Set<String> uris = shortnameToURIs.get(shortName);	
				String it = uris.iterator().next();
				// if this is a declared shortname, then go with it, otherwise prefix it
				Set<String> fullNames = predefinedShortNames.get( shortName );
				if (uris.size() > 1) System.err.println( ">> AMBIGUOUS: " + shortName + " FOR " + uris );
				if (uris.size() == 1 && (fullNames == null || fullNames.contains( it )))
					result.put( it, stripHas(shortName) );
				else {
					for (String uri: uris)
						result.put( uri, prefixFor( NsUtils.getNameSpace(uri) ) + stripHas(shortName) );
				}
			}
			Set <String> terms = result.keySet();
//			System.err.println( ";;; -- defined terms --------------------" );
//			for (String t: terms) System.err.println( ">> term: " + t );
			modelTerms.removeAll( terms );
//			System.err.println( ";;; -- model terms --------------------------");
//			for (String mt: modelTerms) System.err.println( ">> mt: " + mt );
			for (String mt: modelTerms) {
				int cut = Util.splitNamespace( mt );
				String namespace = mt.substring( 0, cut );
				String shortName = mt.substring( cut );
				if (isMagic( namespace )) {
					result.put( mt, stripHas(shortName) );
				} else {					
					result.put( mt, prefixFor( NsUtils.getNameSpace(mt) ) + stripHas(shortName) );
				}
			}
			return result;
		}

		private boolean isMagic(String namespace) {
			if (namespace.equals(DCTerms.getURI())) return true;
			if (namespace.equals("eh:/")) return true;
			if (namespace.equals(SPARQL.NS)) return true;
			if (namespace.equals(ELDA.COMMON.NS)) return true;
			if (namespace.equals(OpenSearch.getURI())) return true;
			if (namespace.equals(DOAP.NS)) return true;
			return false;
		}

		// add mappings to predefined short name
		private void addPredefined( MultiMap<String, String> uriToName,	Map<String, Set<String>> predefinedShortNames, String uri) {
			for (String shortName: uriToName.getAll( uri )) {
				Set <String> already = predefinedShortNames.get( shortName );
				if (already == null) predefinedShortNames.put( shortName, already = new HashSet<String>() );
				already.add( uri );
			}
		}

		private void addAnother(Map<String, Set<String>> shortnameToURIs, String uri, Set<String> shortNames) {			
			for (String sn: shortNames) {
				Set<String> already = shortnameToURIs.get(sn);
				if (already == null) shortnameToURIs.put(sn, already = new HashSet<String>() );
				already.add(uri);				
			}
		}

		private void addURIforShortname(Map<String, Set<String>> shortnameToURIs, String shortName, String uri) {
			Set<String> already = shortnameToURIs.get(shortName);
			if (already == null) shortnameToURIs.put(shortName, already = new HashSet<String>() );
			already.add(uri);
		}

		// compatability (with Puelia) code to handle has-stripping
		private String stripHas(String x) {
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
