/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.util.SplitIRI;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EldaNameUtils {

    /**
     * Answer true iff <code>proposed</code> matches the permitted
     * syntax of short names (which amount to the intersection of Javascript
     * names, to make native JSON handling convenient, and XML element names,
     * to make legal XML).
     */
    public static boolean isLegalShortname(String l) {
        return labelPattern.matcher(l).matches();
    }

    public static Pattern labelPattern = Pattern.compile("[_a-zA-Z][0-9a-zA-Z_]*");

    public static final Pattern prefixSyntax = Pattern.compile("^[A-Za-z][a-zA-Z0-9]*_");

    public static String nameSpace(String uri) {
        return SplitIRI.namespace(uri);
    }

    public static String localName(String uri) {
        return SplitIRI.localname(uri);
    }

    /**
     * Answer N if <code>proposed</code> starts with a legal simple prefix
     * name and then _, where N is the index of the first character past the
     * _, or -1 if it does not so start.
     */
    public static int prefixEndsAt(String proposed) {
        Matcher m = EldaNameUtils.prefixSyntax.matcher(proposed);
        return m.find() ? m.end() : -1;
    }

    /**
     * Answer an escaped version of the local name <code>ln</code>,
     * where any character that is not a letter, digit or _ is replaced
     * by _<hex>_, where <hex> is the hexadecimal code of the character.
     */
    public static String escapeLocalName(String ln) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ln.length(); i++) {
            char c = ln.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            } else {
                sb.append('_');
                sb.append(Integer.toHexString(c));
                sb.append('_');
            }
        }
        return sb.toString();
    }
}
