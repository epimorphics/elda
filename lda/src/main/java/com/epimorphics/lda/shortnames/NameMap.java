/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.shortnames;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Another class maintaining shortnames, this one avoids
    commitments made in Context.
    
    @author chris
*/
public class NameMap {

	private static final Resource ANY = null;
	
	private final MultiMap<String, String> map = new MultiMap<String, String>();
	
	private final PrefixMapping prefixes = PrefixMapping.Factory.create();

	public void load( PrefixMapping pm, Model m ) {
		prefixes.withDefaultMappings( pm );
		load( pm, m.listStatements( ANY, RDFS.label, ANY ) );
		load( pm, m.listStatements( ANY, FIXUP.label, ANY ) );
		load( pm, m.listStatements( ANY, API.name, ANY ) );
	}

	private void load( PrefixMapping pm, StmtIterator si ) {
		while (si.hasNext()) load( pm, si.next() );
	}

	private void load(PrefixMapping pm, Statement s) {
		Resource r = s.getSubject();
		String name = asString( s.getObject().asNode() );
		fullNameToShortName( r.getURI(), name );
	}

	private void fullNameToShortName(String uri, String name) {
		map.add( uri, name );
	}

	private String asString( Node n ) {
		return n.isLiteral() ? n.getLiteralLexicalForm() : n.getURI();
	}

//	public NameMap copy() {
//		NameMap result = new NameMap();
//		result.prefixes.setNsPrefixes( prefixes );
//		result.map.putAll( map );
//		return result;
//	}

	public static class SafeMap {
		
		private static PrefixMapping automatic = PrefixMapping.Factory.create()
			.setNsPrefix( "rdf", RDF.getURI() )
			.setNsPrefix( "rdfs", RDFS.getURI() )
			.setNsPrefix( "xhv", XHV.getURI() )
			;

		private PrefixMapping prefixes = PrefixMapping.Factory.create();
		private Set<String> terms = new HashSet<String>();
		private MultiMap<String, String> uriToName = new MultiMap<String, String>();
		
		public SafeMap( NameMap nm ) {
			prefixes.setNsPrefixes( nm.prefixes );
			prefixes.setNsPrefixes( automatic );
			uriToName.addAll( nm.map );
		}

		public SafeMap load( PrefixMapping pm, Model m ) {
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

		public MultiMap<String, String> result() {
			Map<String, Set<String>> shorts = new HashMap<String, Set<String>>();
			for (String p: terms) {
				String givenShort = uriToName.getOne( p );
				if (givenShort == null) {
					String local = getLocalName(p);
					Set<String> ps = shorts.get(local);
					if (ps == null) shorts.put( local, ps = new HashSet<String>() );
					ps.add( p );
				}
			}
			for (String shortName: shorts.keySet()) {
				Set<String> ps = shorts.get(shortName);
				if (ps.size() == 1) {
					uriToName.add( ps.iterator().next(), t(shortName) );
				} else {
					for (String uri: ps) 
						uriToName.add( uri, prefixFor( getNameSpace(uri) ) + t(shortName) );
				}
			}
			return uriToName;
		}

		// just a HACK to handle has-stripping
		private String t(String x) {
			if (x.startsWith("has") && x.length() > 3) {
				char ch = x.charAt(3);
				if (Character.isUpperCase(ch))
					return Character.toLowerCase(ch) + x.substring(4);
			}
			return x;
		}

		private String getLocalName(String uri) {
			int split = Util.splitNamespace(uri);
			return uri.substring(split);
		}

		private String getNameSpace(String uri) {
			int split = Util.splitNamespace(uri);
			return uri.substring(0, split);
		}

		private String prefixFor( String nameSpace ) {
			String prefix = prefixes.getNsURIPrefix( nameSpace );
			return
				prefix == null ? "none_"
				: isMagic(prefix) ? ""
				: prefix + "_"
				;
		}

		private boolean isMagic(String prefix) {
			return prefix.equals("xhv") || prefix.equals("rdf") || prefix.equals("rdfs");
		}
	}
	
	/**
	    During Stage2, clashing shortnames are resolved rather than permitted.
	*/
	public SafeMap stage2() {
		return new SafeMap( this );
	}
}
