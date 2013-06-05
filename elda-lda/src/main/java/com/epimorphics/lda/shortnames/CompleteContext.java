package com.epimorphics.lda.shortnames;

import static com.hp.hpl.jena.rdf.model.impl.Util.splitNamespace;

import java.util.*;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.util.NameUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;

public class CompleteContext {
	
	final Context context;
	final PrefixMapping prefixes;
	final boolean transcodedNames;
	final boolean allowUniqueLocalnames;
	
	public enum Mode { Transcode, EncodeAny, EncodeIfMultiple } 
	
	public CompleteContext( Mode m, Context context, PrefixMapping prefixes ) {
		this.context = context;
		this.prefixes = prefixes;
		this.transcodedNames = (m == Mode.Transcode);
		this.allowUniqueLocalnames = (m == Mode.EncodeIfMultiple);
	}
	
	public Map<String, String> Do(Model m, PrefixMapping pm) {
		
		Map<String, String> result = new HashMap<String, String>();
	//		
		// force this for ensuring regressions pass. May be unnecessary soon.
		result.put(API.value.getURI(), "value");
		result.put(API.label.getURI(), "label");
	//
		chooseSingleShortnames( result );
		Set<String> modelTerms = loadModelTerms( m, result );				
		
		if (transcodedNames) {
			oldTermHandler(result, modelTerms);
		} else  {
			Map<String, List<String>> localNameToURIs = handlePrefixableNames(pm, result, modelTerms);
			handleSyntheticNames(result, localNameToURIs);
		}
		return result;
	}

	// create synthetic shortnames for those URIs which can't be done
	// any other way.
	private void handleSyntheticNames(Map<String, String> result,
			Map<String, List<String>> localNameToURIs) {
		for (String loc: localNameToURIs.keySet()) {
			List<String> options = localNameToURIs.get(loc);
			
			if (options.size() == 1 && allowUniqueLocalnames) {
				result.put( options.get(0), loc );	
			} else {
				for (int i = 0; i < options.size(); i += 1) {
					result.put( options.get(i), encodeLocalname(loc, i));
				}
			}
		}
	}

	// create prefix_localname shortnames for those URIs that can have them.
	private Map<String, List<String>> handlePrefixableNames
		(PrefixMapping pm, Map<String, String> result, Set<String> modelTerms ) {
		Map<String, List<String>> localNameToURIs = new HashMap<String, List<String>>();
				
		for (String mt: modelTerms) {
			int cut = splitNamespace( mt );
			String ns = mt.substring( 0, cut );
			String ln = mt.substring( cut );
			String prefix = pm.getNsURIPrefix( ns );
		//
			if (prefix != null && NameUtils.isLegalShortname(ln)) {
				result.put( mt, prefix + "_" + ln );
			} else {
				String localName = localNameOf( mt ); // .substring( cut );
				List<String> URIs = localNameToURIs.get( localName );
				if (URIs == null) localNameToURIs.put( localName, URIs = new ArrayList<String>() );
				URIs.add( mt );
			}
		}
		return localNameToURIs;
	}

	private void oldTermHandler(Map<String, String> result,	Set<String> modelTerms) {
		for (String mt: modelTerms) result.put( mt, Transcoding.encode( prefixes, mt ) );
	}

	private Set<String> loadModelTerms(Model m, Map<String, String> result) {
		Set<String> modelTerms = new HashSet<String>();
		for (StmtIterator sit = m.listStatements(); sit.hasNext();) {
			Statement s = sit.next();
			String predicate = s.getPredicate().getURI();
			if (!result.containsKey( predicate)) modelTerms.add( predicate );
			Node o = s.getObject().asNode();
			if (o.isLiteral()) {
				String type = o.getLiteralDatatypeURI();
				if (type != null && !result.containsKey(type)) modelTerms.add( type );
			}
		}
		modelTerms.removeAll( result.keySet() );
		return modelTerms;
	}

	private void chooseSingleShortnames(Map<String, String> result) {
		Map<String, List<String>> shortNames = new HashMap<String, List<String>>();
		for (String key: context.preferredNames()) {
			String uri = context.getURIfromName( key );
			List<String> options = shortNames.get(uri);
			if (options == null) shortNames.put(uri, options = new ArrayList<String>() );
			options.add( key );
		}
//
		for (String uri: shortNames.keySet()) {
			result.put( uri, bestShortname( shortNames.get( uri) ) );
		}
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
	
	// the "best" short name is the shortest. Tiebreak by alphabetical order.
	private String bestShortname( List<String> names ) {
		if (names.size() > 1) Collections.sort(names, compareBySizeThenSpelling);
		// if (names.size() > 1) System.err.println( ">> picking " + names.get(0) + " from " + names );
		return names.get(0);
	}

	private String localNameOf(String uri) {
		int hashPos = uri.indexOf('#');
		int slashPos = uri.lastIndexOf('/');
		int cut = hashPos < 0 ? slashPos : hashPos;
		return uri.substring(cut + 1);
	}

	private static char [] alphaHex = "ABCDEFGHIJKLMNOP".toCharArray();
	
	private String encodeLocalname(String loc, int n) {
		StringBuilder result = new StringBuilder(loc.length() + 5 );
		for (int i = 0; i < loc.length(); i += 1) {
			char ch = loc.charAt(i);
			if (Character.isLowerCase(ch) || Character.isDigit(ch)) {
				result.append( ch );
			} else if (Character.isUpperCase(ch)) {
				result.append( '_' ).append( ch );
			} else if (ch == '_') {
				result.append( '_' ).append( '_' );
			} else if (ch == '-') {
				result.append( 'H' );
			} else {
				result.append( '_' ).append( alphaHex[ch >> 4] ).append( alphaHex[ch & 0xf] );				
			}
		}
		result.append('_').append(n);
		return result.toString();
	}

}
