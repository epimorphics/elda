/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.shortnames;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.lda.exceptions.ReusedShortnameException;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.NameUtils;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.NsUtils;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
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

    static Logger log = LoggerFactory.getLogger(NameMap.class);
    
	/** to make listStatements readable */
	private static final Resource ANY = null;
	
	/** mapping short names to sets of full names */
	private final MultiMap<String, String> mapShortnameToURIs = new MultiMap<String, String>();
	
	/** mapping URIs to some one of their declared shortnames. */
	private final Map<String, String> mapURItoShortName = new HashMap<String, String>();
	
	/** combined prefix mapping from all sources */
	private final PrefixMapping prefixes = PrefixMapping.Factory.create();

	/** 
	 	load a given prefix mapping and model into the map. Looks in the
	 	model for anything with an rdfs:label or api:label property.
	*/
	public void load( PrefixMapping pm, Model m ) {
		prefixes.withDefaultMappings( pm );
		load( true, m.listStatements( ANY, API.label, ANY ) );
		load( false, m.listStatements( ANY, RDFS.label, ANY ) );
		loadClasses( m );
		loadProperties( m );
	}
	
	protected void loadClasses( Model m ) {
		for (Resource t: Context.RES_TYPES_TO_SHORTEN) 
			loadThingsOfType( m, t, false );
	}
	
	protected void loadProperties( Model m ) {
		for (Resource t: Context.PROP_TYPES_TO_SHORTEN)
			loadThingsOfType( m, t, true );
		for (Resource p: m.listSubjectsWithProperty( RDFS.range ).toList())
			loadPropertyIfUndefined( p );
	}

	protected void loadThingsOfType( Model m, Resource type, boolean isProperty ) {
		for (Resource r: m.listSubjectsWithProperty( RDF.type, type ).toList())
			loadThing( r, isProperty );
	}
	
	private void loadPropertyIfUndefined(Resource p) {
		ContextPropertyInfo cpi = uriToPropertyInfo.get(p.getURI());
		if (cpi == null) loadThing( p, true );
	}
	
	protected final Map<String, ContextPropertyInfo> uriToPropertyInfo = new HashMap<String, ContextPropertyInfo>();
	
	protected void loadThing( Resource r, boolean isProperty ) {
		if (r.isURIResource()) {
			String uri = r.getURI();
			String shortName = r.getLocalName();
			// if (!mapShortnameToURIs.containsKey(shortName)) mapShortnameToURIs.add(shortName, uri );
			if (isProperty) {
				ContextPropertyInfo cpi = uriToPropertyInfo.get(uri);
				if (cpi == null) uriToPropertyInfo.put(uri,  cpi = new ContextPropertyInfo( uri, null ) );
				if (r.hasProperty( RDF.type, API.Multivalued)) cpi.setMultivalued(true);
			    if (r.hasProperty( API.multiValued )) cpi.setMultivalued( r.getProperty( API.multiValued ).getBoolean() );
			    if (r.hasProperty( API.structured )) cpi.setStructured( r.getProperty( API.structured ).getBoolean() );
			    if (r.hasProperty( RDF.type, API.Hidden)) cpi.setHidden(true);
			    if (r.hasProperty( RDF.type, OWL.ObjectProperty)) cpi.setType(OWL.Thing.getURI());
			    if (r.hasProperty( RDFS.range ) && cpi.getType() == null) cpi.setType( RDFUtil.getStringValue(r, RDFS.range) );
			}
		}
	}

	public ContextPropertyInfo getInfo(Property p) {
		String uri = p.getURI();
		ContextPropertyInfo cpi = uriToPropertyInfo.get(uri);		
		if (cpi == null) {
			String shortName = p.getLocalName();
			uriToPropertyInfo.put(uri,  cpi = new ContextPropertyInfo( uri, shortName ) );
		}
		return cpi;
	}

	/**
	    A mutable copy of the uri-to-property-info map.
	*/
	public Map<String, ContextPropertyInfo> getInfoMap() {
		Map<String, ContextPropertyInfo> result = new HashMap<String, ContextPropertyInfo>();
		for (Map.Entry<String, ContextPropertyInfo> me: uriToPropertyInfo.entrySet()) {
			result.put(me.getKey(), me.getValue().clone() );
		}
		return result;
	}
	
	/**
	    Answer the current mapping from URIs to short names. Immutable.
	*/
	public Map<String, String> getURItoShortnameMap() {
		return Collections.unmodifiableMap( mapURItoShortName );
	}
	
	public Set<String> allShortNames() {
		return new HashSet<String>( mapShortnameToURIs.keySet() );
	}
	
	public String getNameForURI(String uri) {
		String sn = mapURItoShortName.get(uri);
//		for (String x: mapShortnameToURIs.keySet()) {
//			if (mapShortnameToURIs.getAll(x).contains( uri )) {
//			}
//		}
		return sn;
	}
	
	public ContextPropertyInfo getPropertyByName(String sn) {
		String uri = mapShortnameToURIs.getOne(sn);
		return uriToPropertyInfo.get(uri);
	}
	
	public String getURIfromName(String sn) {
		return mapShortnameToURIs.getOne(sn);
	}
	
	/**
	    load vocabulary elements from m that are not already
	    defined in this NameMap.
	*/
	public void loadIfNotDefined( PrefixMapping pm, Model m ) {
		NameMap inner = new NameMap();
		inner.load( pm, m );
		inner.done();
	//
		for (String shortName: inner.mapShortnameToURIs.keySet()) {
			if (!mapShortnameToURIs.containsKey(shortName)) {
				// System.err.println( ">> not already defined: " + shortName );
				mapShortnameToURIs.add(shortName, inner.mapShortnameToURIs.getAll( shortName ) );
			} else {
				// System.err.println( ">> already defined: " + shortName );				
			}
		}
	}

	private void load( boolean strongBinding, StmtIterator si ) {
		while (si.hasNext()) load( strongBinding, si.next() );
	}

	/**
	    S is (r, someLabelPredicate, someLabel). Give the resource r the
	    shortname someLabel. The binding may be strong (API.label, done first)
	    or weak (RDFS.label, done after all the API.label's). A weak label
	    is ignored if there are any strong labels. Attempted strong bindings to
	    bad shortnames generate a warning message.
	*/
	private void load( boolean strongBinding, Statement s ) {
		Resource r = s.getSubject();
		if (r.isURIResource()) {
			String shortName = asString( s.getObject() );
			String uri = r.getURI();
			if (strongBinding || mapShortnameToURIs.getAll( shortName ).isEmpty()) {
				mapShortnameToURIs.add( shortName, uri );
				if (!NameUtils.isLegalShortname( shortName )) {
					// log.warn( "ignored bad shortname " + shortName + " for " + s.getModel().shortForm( uri ) ); 	
				}
			}
		}		
//			if (NameUtils.isLegalShortname( shortName )) {
//				if (strongBinding || mapShortnameToURIs.getAll( shortName ).isEmpty())
//					mapShortnameToURIs.add( shortName, uri );
//			}
//			else if (strongBinding)
//				log.warn( "ignored bad shortname " + shortName + " for " + s.getModel().shortForm( uri ) ); 
//			}
	}
	
	/**
	    No more updates to make: check what we've got.
	*/
	public void done() {
//		System.err.println( ">> DONING -------------------------" );
		for (String shortName: mapShortnameToURIs.keySet()) {
			Set<String> uris = mapShortnameToURIs.getAll( shortName );
			if (uris.size() > 1) throw new ReusedShortnameException( shortName, uris );
			mapURItoShortName.put( uris.iterator().next(), shortName );
		}
	//
		Map<String, Boolean> potentials = new HashMap<String, Boolean>();
		for (Map.Entry<String, String> e: mapURItoShortName.entrySet()) {
			String uri = e.getKey(), ln = NsUtils.getLocalName(uri);
//			System.err.println( ">> maybe: " + ln );
			if (!mapShortnameToURIs.containsKey(ln)) {
//				System.err.println( ">>   candidate!" );
				Boolean b = potentials.get( ln );
				if (b == null) potentials.put( ln, true );
				else if (b) potentials.put( ln, false );
//				System.err.println( ">>   potentials: " + potentials.get(ln) );
			}
		}
		for (Map.Entry<String, String> e: mapURItoShortName.entrySet()) {
			String sn = e.getValue();
			if (potentials.containsKey(sn) && potentials.get(sn)) {
//				System.err.println( ">> adding: " + sn );
				mapShortnameToURIs.add( sn, e.getKey() );
			}
		}
	//
		for (Map.Entry<String, ContextPropertyInfo> e: uriToPropertyInfo.entrySet()) {
			String uri = e.getKey(), sn = mapURItoShortName.get( uri );
			String ln = NameUtils.localName(uri);
			if (sn == null && NameUtils.isLegalShortname( ln )) sn = ln;
//			if (sn == null) sn = ln;
			if (sn == null) log.warn( "property " + uri + " has no legal shortname." );
			e.getValue().setName( sn );
		}
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
	public Stage2NameMap stage2() {
		return new Stage2NameMap( this );
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
			.setNsPrefix( "doap", DOAP.getURI() )
			;

		/** The combined namespace prefixes from all models. */
		private PrefixMapping prefixes = PrefixMapping.Factory.create();
		
		/** Terms which arrive only in the model and hence are subordinate
		 	to terms that have been define explicitly.
		 */
		private Set<String> modelTerms = new HashSet<String>();
		
		/** the mapping from full URIs to all their allowed shortnames.*/
		private Map<String, String> uriToName = new HashMap<String, String>();
		
		/** Construct a Stage2 map from a NameMap. */
		public Stage2NameMap( NameMap nm ) {
			this.prefixes.setNsPrefixes( nm.prefixes );
			this.prefixes.setNsPrefixes( automatic );
			this.uriToName.putAll( nm.mapURItoShortName );
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
		    disambiguate. If the local name isn't a legal shortname, the
		    Transcoder will escape it.
		*/
		
		public Map<String, String> constructURItoShortnameMap() {
			Map<String, String> mapURItoShortName = new HashMap<String, String>();
			loadMagic( mapURItoShortName );
			mapURItoShortName.putAll( uriToName );
			modelTerms.removeAll( mapURItoShortName.keySet() );
			for (String mt: modelTerms) {
				int cut = Util.splitNamespace( mt );
				String namespace = mt.substring( 0, cut );
				String shortName = mt.substring( cut );
				if (namespace.equals( "eh:/" )) {
					// TODO testing hack: fix and remove.
					mapURItoShortName.put( mt, shortName );
				} else {						
					mapURItoShortName.put( mt, Transcoding.encode( prefixes, mt ) );
				}
			}
			return mapURItoShortName;
		}

		private void loadMagic( Map<String, String> map ) {
			map.put( API.value.getURI(), "value" );
			map.put( API.label.getURI(), "label" );
		}		
	}
}
