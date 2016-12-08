/******************************************************************
 * File:        NameUtils.java
 * Created by:  Dave Reynolds
 * Created on:  10 Apr 2011
 *
 * (c) Copyright 2011, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *****************************************************************/

package com.epimorphics.lda.testing.utils;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import com.epimorphics.util.EpiException;


/**
 * Utilities for checking and converting names to forms safely
 * usable in different circumstances.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class NameSupport {

    /**
     * Convert an arbitrary name to name safe to use as a file name or URL path slug.
     * Not reversible.
     */
    public static String safeName(String name) {
        return name.replaceAll("[^@$a-zA-Z0-9_\\.\\-~]+", "_");
    }
    
    /**
     * Convert an arbirary name, such as a CSV column name to something usable as a
     * variable name in an expression as well as a path segment, normalizing the case.
     */
    public static String safeVarName(String name) {
        return name.replaceAll("[^@$a-zA-Z0-9_]+", "_").toLowerCase();
    }

    /**
     * Convert an arbitrary name to a URI which can be used in metadata
     * descriptions of the named object. If the name already looks like
     * a URI then use that. If not then mint a URI based on the address of the
     * server and the given name of the "type" of resource. This will
     * look like:
     * <pre>
     *  {intendedBaseURL}/modal/{type}/{safe-version-of-name}
     * </pre>
     */
    public static String uriFor(String name, String serverURI, String type) {
        if (isURI(name)) {
            return name;
        } else {
            // not a valid IRI
            return serverURI + "/modal/" + type + "/" + safeName(name);
        }
    }

    /**
     * Test if the given name is a legal URI.
     */
    public static boolean isURI(String name) {
        IRI testing = IRIFactory.jenaImplementation().create(name);
        return ! testing.hasViolation(false);
    }


    /**
     * Encode a resource URI into a unique label which can be used
     * as a URI segment into a REST API. Not reversible.
     */
    public static String idFromURI(String uri) {
        // TODO decide on handling of non-ascii chars in id generation
        // use _ instead of %-encode because %-encode of "/" causes problems for e.g. Grizzly
        return uri.replaceFirst("http(s?)://", "").replaceAll("[^-\\.@\\w]", "_");
    }

    /**
     * Convert a name to a safe name in a reversible fashion by
     * (very) conservative percent-encoding of the UTF-8 version.
     */
    public static String encodeSafeName(String name) {
        if (name == null) return null;
        try {
            StringBuilder encode = new StringBuilder();
            for (byte b : name.getBytes("UTF-8")) {
                char c = (char)b;
                if (c == '_' || c == '-' | c == '@' | c == '.' | Character.isLetterOrDigit(c)) {
                    encode.append(c);
                } else {
                    encode.append("%");
                    encode.append( Integer.toHexString(b) );
                }
            }
            return encode.toString();
        } catch (UnsupportedEncodingException e) {
            throw new EpiException(e);
        }
    }

    /**
     * Decode an encoded safe name
     */
    public static String decodeSafeName(String name) {
        byte[] decode = new byte[ name.length() ];
        int p = 0;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '%') {
                decode[p++] = (byte) Integer.parseInt(name.substring(i+1, i+3), 16);
                i += 2;
            } else {
                decode[p++] = (byte) c;
            }
        }
        byte[] after = new byte[p];
        System.arraycopy(decode, 0, after, 0, p);
        try {
            return new String(after, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new EpiException(e);
        }
    }

    /**
     * Generate a random name for something of the given type, result
     * will currently look like: {type}-{uuid}
     */
    public static String newName(String type){
        return type + "-" + UUID.randomUUID();
    }

    /**
     * Generate a random URI for something of the given type, result
     * will currently look like: {server}/modal/{type}/{uuid}
     */
    public static String newURI(String type, String serverURI){
        return serverURI + "/modal/" + type + "/" + UUID.randomUUID();
    }

   /**
    * Extract an integer from a parameter object, with default
    */
    public static int asInt(Object param, int defaultint) {
        if (param == null) {
            return defaultint;
        }
        if (param instanceof Number) {
            return ((Number)param).intValue();
        }
        try {
            return Integer.parseInt(param.toString());
        } catch (NumberFormatException e) {
            return defaultint;
        }
    }

    /**
     * Normalize a relative file path to unix syntax.
     * I.e. on widows will replace "\" by "/"
     */
    public static String normalizeFilepath(String path) {
        // Note: Using hardwired "\" rather than File.separator so
        // that the normalization will happen even if we are running on UNIX
        // but have been given a parameter generated while on windows
        return path.replaceAll("\\\\", "/");
    }

    /**
     * Strip any leading "file:" off a URL string
     */
    public static String removeFilePrefix(String f) {
        if (f.startsWith("file:")) {
            return f.substring(5);
        } else {
            return f;
        }
    }

    /**
     * Find the last path segment in a URL or unix path.
     */
    public static String lastSegment(String f) {
        return splitAfterLast(f, "/");
    }

    /**
     * Normalize a URI by stripping any final "/"
     */
    public static String stripLastSlash(String u) {
        if (u.endsWith("/")) {
            return u.substring(0, u.length()-1);
        }
        return u;
    }

    /**
     * Normalize a URI by ensuring there is a final "/"
     */
    public static String ensureLastSlash(String u) {
        if (u.endsWith("/")) {
            return u;
        } else {
            return u + "/";
        }
    }

    /**
     * Remove a trailing .xxx extension from a file name
     */
    public static String removeExtension(String f) {
        return splitBeforeLast(f, ".");
    }

    /**
     * Find the .xxx extension of a file name
     */
    public static String extension(String f) {
        return splitAfterLast(f, ".");
    }

    /**
     * Return segment of a string after the last occurrence of "at"
     */
    public static String splitAfterLast(String filename, String at) {
        int split = filename.lastIndexOf(at);
        if (split == -1) {
            return filename;
        } else {
            return filename.substring(split + 1);
        }
    }


    /**
     * Return segment of a string before the last occurrence of "at"
     */
    public static String splitBeforeLast(String filen, String at) {
        int split = filen.lastIndexOf(at);
        if (split == -1) {
            return filen;
        } else {
            return filen.substring(0, split);
        }

    }
}
