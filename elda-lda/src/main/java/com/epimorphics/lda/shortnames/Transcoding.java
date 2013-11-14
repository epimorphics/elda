package com.epimorphics.lda.shortnames;

import com.epimorphics.util.NameUtils;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
    <p>
    Transcoding deals with the encoding of arbitrary URIs within the syntax
    of short names. This is necessary when rendering arbitrary predicates into
    XML, since legal NCNames need not be legal shortnames, and there may be
    no prefix available for the namespace. (Allocating and remembering
    prefixes would be stateful and would mean that the set of legal shortnames
    would depend on what renderings had been done since the last server
    restart.)
    </p>
    
    <p>
    What we do is to define the /light encoding/ as: leave the lower-case 
    letters alone and prefix any upper-case letter or "_" with "_". 
    The well-known special characters (:/.-?&#) are translated to a mnemonic 
    upper-case letter, and the other special characters are translated to _HH 
    where the HH are (lower-case) hex digits. Additionally, a leading ://
    is converted to X (because CSS looks wrong) and a Z is inserted before
    the first character of the encoded local name. This encoding is reversible.
    </p>
    
    <p>For a URI N:L where N is the namespace and L is the local name, N has
    prefix P, and L is a legal shortname, we encode as P_L.
    </p>
    
    <p>If L isn't a legal shortname, we encode as pre_P_L', where L' is
    the light encoding of L.</p>
    
    <p>In the remaining case, where there's no prefix for N, we give up
    and generate unknown_N'L'.</p>
    
    <p>
   	This requisitions the two pseudo-prefix names 'pre' and 'unknown',
   	but we'll have to live with that.
    </p>
*/
public class Transcoding {

	/**
	    Answer the decoding (expansion) of the given shortname if it is in
	    transcoded form "unknown_ENCODED" or "pre_APREFIX_ENCODED", or if it is
	    of the form prefixName_localName, or null if it is not.
	*/
	public static String decode( PrefixMapping pm, String shortName ) {
		if (shortName.startsWith( "uri_")) return decodeAny(shortName.substring(4));
		if (shortName.startsWith( "unknown_")) return decodeLightly(shortName.substring(8));
		if (shortName.startsWith( "pre_" )) return decodeMarkedPrefix(pm, shortName.substring(4));
		return decodeMaybePrefixed(pm, shortName);
	}

	public static String decodeMaybePrefixed(PrefixMapping pm, String shortName) {
		int cut = NameUtils.prefixEndsAt( shortName );
		if (cut < 0) return null;
		String prefix = shortName.substring(0, cut - 1);
		String nameSpace = pm.getNsPrefixURI( prefix );
		if (nameSpace == null) return null;
		return nameSpace + shortName.substring(cut) ; 
	}

	private static String decodeMarkedPrefix( PrefixMapping pm, String prefix_encoded ) {
		int ubar = prefix_encoded.indexOf('_');
		if (ubar < 0) return null;
		String nameSpace = pm.getNsPrefixURI( prefix_encoded.substring(0, ubar) );
		if (nameSpace == null) return null;
		return nameSpace + decodeLightly( prefix_encoded.substring(ubar + 1) );
	}
	
	private static String decodeAny( String shortName ) {
    	StringBuilder result = new StringBuilder( shortName.length() );
    	char previous = 0;
    	for(int i = 0, limit = shortName.length() ; i < limit ; i += 1) { 
    	    char c = shortName.charAt(i);
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
		return c - 'a' + 10;
	}

	/**
	    Encode the URI string <code>any</code> into a "short"name.
	    If any can be expressed as a legal shortname pre_local where
	    pre is the prefix for any's namespace and local is the
	    local name of any, return that. Otherwise, if the local
	    name isn't a legal shortname but a prefix exists, return
	    pre_thatPrefix_localnameEncoded. Otherwise, return
	    uri_theEntireURIencoded.
	*/
	public static String encode( PrefixMapping pm, String any ) {
		int cut = Util.splitNamespace( any );
		String ns = any.substring( 0, cut );
		String ln = any.substring( cut );
		String prefix = pm.getNsURIPrefix( ns );
		return
			prefix == null ? "unknown_" + encodeLightly(true, any) 
			: NameUtils.isLegalShortname( ln ) ? prefix + "_" + ln
			: "pre_" + prefix + "_" + encodeLightly(false, ln)
			;
	}

	public static String decodeLightly( String encoded ) {
		StringBuilder sb = new StringBuilder();
		char pending = 0;
		boolean underbar = false, inHex = false;
		for (int i = 0, limit = encoded.length() ; i < limit ; i += 1) {
    		char c = encoded.charAt(i);
    		if (inHex) {
    			sb.append( (char)( (unhex(pending) << 4) | unhex(c) ) );
    			inHex = false;
    		} else if (underbar) {
    			if (Character.isUpperCase(c) || c == '_') {
    				sb.append(c);
    			} else {
    				pending = c;
    				inHex = true;
    			}
    			underbar = false;
    		} else if (c == 'S') {
    			sb.append( '/');
    		} else if (c == 'C') {
    			sb.append( ':');    			
    		} else if (c == 'D') {
    			sb.append( '.');    			
    		} else if (c == 'H') {
    			sb.append( '#');
    		} else if (c == 'M') {
    			sb.append( '-');
    		} else if (c == 'A') {
    			sb.append( '&' );
    		} else if (c == 'Q') {
    			sb.append( '?' );
    		} else if (c == 'E') {
    			sb.append( '=' );
    		} else if (c == 'X') {
    			sb.append( "://" );
    		} else if (c == 'Z') {
    			// Nothing -- it's the localName marker
    		} else if (c == '_') {
    			underbar = true;
    		} else {
    			sb.append( c );
    		}
		}		
//		System.err.println( ">> decoded " + encoded );
//		System.err.println( ">> to:     " + sb );
		return sb.toString();
	}
	
	private static String encodeLightly(boolean useZ, String any) {
		StringBuilder sb = new StringBuilder();
		if (any.startsWith("http://")) {
			sb.append("httpX");	any = any.substring(7);
		}
		int lastSlash = any.lastIndexOf( '/' );
		int firstHash = any.indexOf( '#' );
		int localNameStart = (firstHash < 0 ? lastSlash : firstHash) + 1;
		for (int i = 0, limit = any.length() ; i < limit ; i += 1) {
			if (i == localNameStart && useZ) sb.append( 'Z' );
    		char c = any.charAt(i);
    		if ('a' <= c && c <= 'z' || '0' <= c && c <= '9') {
    			sb.append( c );
    		} else if (c == '/') {
    			sb.append( 'S' );
    		} else if (c == ':') {
    			sb.append( 'C' );
    		} else if (c == '.') {
    			sb.append( 'D' );
    		} else if (c == '#') {
    			sb.append( 'H' );
    		} else if (c == '-') {
    			sb.append( 'M' );
    		} else if (c == '&') {
    			sb.append( 'A' );
    		} else if (c == '?') {
    			sb.append( 'Q' );
    		} else if (c == '=') {
    			sb.append( 'E' );
    		} else if (c == '_') {
    			sb.append( '_' ).append( '_' );
    		} else if ('A' <= c && c <= 'Z') {
    			sb.append( '_' ).append( c );
    		} else {
    			sb.append( '_' ).append( hex[c >> 4] ).append( hex[c & 0xf] );
    		}
		}
		return sb.toString();
	}	

	private static char [] hex = "0123456789abcdef".toCharArray();

}
