package com.epimorphics.lda.shortnames;

import static com.hp.hpl.jena.rdf.model.impl.Util.splitNamespace;

import java.util.*;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.NameUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;

public class CompleteContext {
	
	final Context context;
	final PrefixMapping prefixes;
	final boolean transcodedNames;
	final boolean allowUniqueLocalnames;
	final Model model = ModelFactory.createDefaultModel();
	
	final Map<String, String> uriToShortname = new HashMap<String, String>();
	
	public enum Mode { 
		RoundTrip, PreferPrefixes, PreferLocalnames ;
	
		public int n() { return 27; }		
		
		public static Mode decode( Resource root, Mode defaultMode ) {
			if (root == null) return defaultMode;
			Statement s = root.getProperty( EXTRAS.shortnameMode );
			if (s == null) return defaultMode;
			String modeString = s.getString();
			return
				modeString.equals(EXTRAS.preferPrefixes) ? CompleteContext.Mode.PreferPrefixes
				: modeString.equals(EXTRAS.preferLocalnames) ? CompleteContext.Mode.PreferLocalnames
				: modeString.equals(EXTRAS.roundTrip) ? CompleteContext.Mode.RoundTrip
				: defaultMode
				;
		}
	} 
	
	public CompleteContext( Mode m, Context context, PrefixMapping prefixes ) {
		this.context = context;
		this.prefixes = prefixes;
		this.transcodedNames = (m == Mode.RoundTrip);
		this.allowUniqueLocalnames = (m == Mode.PreferLocalnames);
	}
	
	static class SplitURI {
		final String uri;
		final String ns;
		final String ln;
		
		SplitURI(String uri, String ns, String ln) {
			this.uri = uri;
			this.ns = ns;
			this.ln = ln;
		}
		
		static SplitURI create(String uri) {
			int cut = splitNamespace( uri );
			String ns = uri.substring( 0, cut );
			String ln = uri.substring( cut );
			return new SplitURI(uri, ns, ln);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof SplitURI && same( (SplitURI) other );
		}
		
		@Override public int hashCode() {
			return ns.hashCode() ^ ln.hashCode();
		}

		private boolean same(SplitURI other) {
			return ns.equals(other.ns) && ln.equals(other.ln);
		}
	}

	public CompleteContext include(Model m) {
		model.add(m);
		model.withDefaultMappings( m );
		return this;
	}
	
	public Map<String, String> Do1(Model m) {
		return include( m ).Do();
	}
	
	public Map<String, String> Do(Model m, PrefixMapping pm) {
		model.setNsPrefixes( pm );
		return include( m ).Do();
	}

	public Map<String, String> Do() {
		uriToShortname.clear();
	//
		uriToShortname.put(API.value.getURI(), "value");
		uriToShortname.put(API.label.getURI(), "label");
	//
		pickPreferredShortnames();
		Set<SplitURI> modelTerms = loadModelTerms( uriToShortname.keySet() );
		
		if (transcodedNames) {
			oldTermHandler( uriToShortname, modelTerms );
		} else {
			extractPrefixedAndUniqueShortnames(	modelTerms );
			extractHashedShortnames( modelTerms );
		}
		return uriToShortname;
	}

	/**
	    For those URIs which can be uniquely mapped to an "encoded" localname
	    as their shortname, add this mapping to uriToShortname.
	*/
	private void extractHashedShortnames( Set<SplitURI> modelTerms ) {
		Set<SplitURI> mtsRemoved = new HashSet<SplitURI>();
		for (SplitURI mt: modelTerms) {
			String sn = encodeLocalname(mt.ns, mt.ln);
			if (!uriToShortname.containsValue(sn)) {
				uriToShortname.put(mt.uri,  sn);
				mtsRemoved.add(mt);
			}
		}
		modelTerms.removeAll( mtsRemoved );	
	}

	/**
	    Extract URIs that are uniquely identified by their local name, or
	    by their local name prefixed by a prefix for their URI.
	*/
	private void extractPrefixedAndUniqueShortnames( Set<SplitURI> modelTerms ) {
		Map<String, List<SplitURI>> localNameToURIs = new HashMap<String, List<SplitURI>>();
		
		for (SplitURI mt: modelTerms) {			
			String ln = mt.ln;
			if (NameUtils.isLegalShortname(ln) && !uriToShortname.containsKey( ln )) {
				List<SplitURI> terms = localNameToURIs.get( ln );
				if (terms == null) localNameToURIs.put( ln, terms = new ArrayList<SplitURI>() );
				terms.add( mt );
			}
		}
		
		if (allowUniqueLocalnames) {
			Set<String> lnsRemoved = new HashSet<String>();
			
			for (String ln: localNameToURIs.keySet()) {
				List<SplitURI> terms = localNameToURIs.get(ln);
				if (terms.size() == 1) {
					SplitURI term = terms.get(0);
					uriToShortname.put( term.uri, ln );
					modelTerms.remove( term );
					lnsRemoved.add( ln );
				} 
			}
			
			for (String ln: lnsRemoved) localNameToURIs.remove( ln );
		}
		
		Set<SplitURI> mtsRemoved = new HashSet<SplitURI>();
		
		for (SplitURI mt: modelTerms) {
			String prefix = model.getNsURIPrefix( mt.ns );
			if (prefix != null) {
				String sn = prefix + "_" + mt.ln;
				if (!uriToShortname.containsValue(sn)) {
					uriToShortname.put( mt.uri, sn );
					mtsRemoved.add( mt );
				}
			}
		}
		
		modelTerms.removeAll( mtsRemoved );
	}
	
	/**
	    For each URI with any shortnames, pick the "best" shortname and
	    add `shortName -> URI` to the result map.
	*/
	private void pickPreferredShortnames() {
		Map<String, List<String>> shortNames = new HashMap<String, List<String>>();
		for (String key: context.preferredNames()) {
			String uri = context.getURIfromName( key );
			List<String> options = shortNames.get(uri);
			if (options == null) shortNames.put(uri, options = new ArrayList<String>() );
			options.add( key );
		}
//
		for (String uri: shortNames.keySet()) {
			uriToShortname.put( uri, bestShortname( shortNames.get(uri) ) );
		}
	}	
	
//	public Map<String, String> Do1(Model m, PrefixMapping pm) {
//		
//		Map<String, String> uriToShortname = new HashMap<String, String>();
//	//		
//		// force this for ensuring regressions pass. May be unnecessary soon.
//		uriToShortname.put(API.value.getURI(), "value");
//		uriToShortname.put(API.label.getURI(), "label");
//	//
//		pickPreferredShortnames( uriToShortname );
//		Set<String> modelTerms = loadModelTerms( m, uriToShortname.keySet() );				
//		
//		if (transcodedNames) {
//			oldTermHandler(uriToShortname, modelTerms);
//		} else  {
//			Map<String, List<String>> localNameToURIs = handlePrefixableNames(pm, uriToShortname, modelTerms);
//			handleSyntheticNames(uriToShortname, localNameToURIs);
//		}
//		return uriToShortname;
//	}

//	// create synthetic shortnames for those URIs which can't be done
//	// any other way.
//	private void handleSyntheticNames(Map<String, String> result, Map<String, List<String>> localNameToURIs) {
//		for (String loc: localNameToURIs.keySet()) {
//			List<String> options = localNameToURIs.get(loc);
//			
//			if (options.size() == 1 && allowUniqueLocalnames) {
//				result.put( options.get(0), loc );	
//			} else {
//				for (int i = 0; i < options.size(); i += 1) {
//					result.put( options.get(i), encodeLocalname(loc, options.get(i) ) );
//				}
//			}
//		}
//	}

//	// create prefix_localname shortnames for those URIs that can have them.
//	private Map<String, List<String>> handlePrefixableNames
//		(PrefixMapping pm, Map<String, String> result, Set<String> modelTerms ) {
//		Map<String, List<String>> localNameToURIs = new HashMap<String, List<String>>();
//				
//		for (String mt: modelTerms) {
//			int cut = splitNamespace( mt );
//			String ns = mt.substring( 0, cut );
//			String ln = mt.substring( cut );
//			String prefix = pm.getNsURIPrefix( ns );
//		//
//			if (prefix != null && NameUtils.isLegalShortname(ln)) {
//				result.put( mt, prefix + "_" + ln );
//			} else {
//				String localName = localNameOf( mt ); // .substring( cut );
//				List<String> URIs = localNameToURIs.get( localName );
//				if (URIs == null) localNameToURIs.put( localName, URIs = new ArrayList<String>() );
//				URIs.add( mt );
//			}
//		}
//		return localNameToURIs;
//	}

	private void oldTermHandler(Map<String, String> result,	Set<SplitURI> modelTerms) {
		for (SplitURI mt: modelTerms) result.put( mt.uri, Transcoding.encode( prefixes, mt.uri ) );
	}

	/**
	    Return the set of terms (URIs) seen in the model as predicates or
	    datatypes, excluding those that have already been seen. These are
	    the terms that will need to be given shortnames.
	*/
	private Set<SplitURI> loadModelTerms(Set<String> seenTerms) {
		Set<SplitURI> modelTerms = new HashSet<SplitURI>();
		for (StmtIterator sit = model.listStatements(); sit.hasNext();) {
			Statement s = sit.next();
			String predicate = s.getPredicate().getURI();
			
//			if (predicate.equals("http://purl.org/linked-data/api/vocab#sparqlEndpoint"))
//				System.err.println( ">> YAY HAY HO!" );
			
			if (!seenTerms.contains( predicate )) 
				modelTerms.add( SplitURI.create(predicate) );
			Node o = s.getObject().asNode();
			if (o.isLiteral()) {
				String type = o.getLiteralDatatypeURI();
				if (type != null && !seenTerms.contains(type)) 
					modelTerms.add( SplitURI.create(type) );
			}
		}
		return modelTerms;
	}

	// order by badness first, then shortest, then alphabetical
	private static final Comparator<String> compareBySizeThenSpelling = new Comparator<String>() {

		@Override public int compare(String a, String b) {
			int a_bad = countBad(a), b_bad = countBad(b);
			int result = a_bad - b_bad;
			if (result == 0) result = a.length() - b.length();
			if (result == 0) result = a.compareTo(b);
			return result;
		}

		private int countBad(String name) {
			int badness = 0;
			for (int i = 0; i < name.length(); i += 1)
				if (!good(name.charAt(i))) badness += 1;
			return badness;
		}

		private boolean good(char ch) {
			return Character.isLetterOrDigit(ch) || ch == '_';
		}
		
	};
	
	/**
	    Retunr the "best" short name, which we take to be the shortest. 
	    Tiebreak by alphabetical order.
	*/
	private String bestShortname( List<String> names ) {
		if (names.size() > 1) Collections.sort(names, compareBySizeThenSpelling);
		return names.get(0);
	}
	
	/**
	    Return the local name, plus an '_', plus an encoding of the namespace --
	    we pick the low-order four digits of its hashcode.
	*/
	private String encodeLocalname(String ns, String loc) {
		return new StringBuilder(loc.length() + 5 )
			.append( loc )
			.append('_')
			.append( Math.abs( ns.hashCode()) % 10000 )
			.toString()
			;
	}

}
