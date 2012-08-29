package com.epimorphics.lda.shortnames;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.rdf.model.impl.Util;

public class Transcoding {

	public static String decode( PrefixMapping pm, String shortName ) {
		if (shortName.startsWith( "uri_")) return decodeAny(shortName.substring(4));
		if (shortName.startsWith( "pre_" )) return decodePrefix(pm, shortName.substring(4));
		return null;
	}

	private static String decodePrefix( PrefixMapping pm, String prefix_encoded ) {
		int ubar = prefix_encoded.indexOf('_');
		if (ubar < 0) return null;
		String nameSpace = pm.getNsPrefixURI( prefix_encoded.substring(0, ubar) );
		if (nameSpace == null) return null;
		return nameSpace + decodeAny( prefix_encoded.substring(ubar + 1) );
	}
	
	private static String decodeAny( String s ) {
    	StringBuilder result = new StringBuilder( s.length() );
    	char previous = 0;
    	for(int i = 0, limit = s.length() ; i < limit ; i += 1) { 
    	    char c = s.charAt(i);
    	    if ('A' <= c && c <= 'F' || '0' <= c && c <= '9') {
    	    	if (previous == 0) {
    	    		previous = c;
    	    	} else {
    	    		result.append( (char)( (unhex(previous) << 4) | unhex(c) ) );
    	    		previous = 0;
    	    	}
    	    } else {
    	    	result.append( c );
    	    }
    	}
		return result.toString();
	}

	private static int unhex(char c) {
		if ('0' <= c && c <= '9') return c - '0';
		return c - 'A' + 10;
	}

	public static String encode( PrefixMapping pm, String any ) {
		int cut = Util.splitNamespace( any );
		String ns = any.substring( 0, cut );
		String ln = any.substring( cut );
		String prefix = pm.getNsURIPrefix( ns );
		return
			prefix == null ? "uri_" + encodeAny( any )
			: NameMap.checkLegal( ln ) ? prefix + "_" + ln
			: "pre_" + prefix + "_" + encodeAny( ln )
			;
	}
	
	private static char [] hex = "0123456789ABCDEF".toCharArray();
	
	public static String encodeAny( String any ) {
		StringBuilder result = new StringBuilder();
    	for(int i = 0, limit = any.length() ; i < limit ; i += 1) {
    		char c = any.charAt(i);
    		if ('a' <= c && c <= 'z') 
    			result.append( c );
    		else
    			result.append( hex[c >> 4] ).append( hex[c & 0xf] );
    	}
		return result.toString();
	}

}
