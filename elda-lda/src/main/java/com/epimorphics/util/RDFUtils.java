/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        RDFUtils.java
    Created by:  Dave Reynolds
    Created on:  1 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Random collection of RDF Utilities that should really be in Jena.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class RDFUtils {

	/**
	    An immutable empty prefix mapping.
	*/
	public static final PrefixMapping noPrefixes = PrefixMapping.Factory.create().lock();

    /**
     * Return one of the values of the property on the resource in string form.
     * If there are no values return the defaultValue. If the value is not
     * a String but is a literal return it's lexical form. If it is a resource
     * return it's URI. 
     */
    public static String getStringValue(Resource r, Property p, String defaultValue) {
        Statement s = r.getProperty(p);
        if (s == null) {
            return defaultValue;
        } else {
            return getLexicalForm( s.getObject() );
        }
    }

    /**
     * Return one of the values of the property on the resource in string form.
     * If there are no values return null.
     */
    public static String getStringValue(Resource r, Property p) {
        return getStringValue(r, p, null);
    }
    
    /**
     * Return the lexical form of a node. This is the lexical form of a
     * literal, the URI of a URIResource or the annonID of a bNode.
     */
    public static String getLexicalForm(RDFNode value) {
        if (value.isLiteral()) {
            return ((Literal)value).getLexicalForm();
        } else if (value.isURIResource()) {
            return ((Resource)value).getURI();
        } else {
            return value.toString();
        }
    }
    
    /**
     * Return the value of a resource on a property as a resource, or
     * null if there isn't a resource value.
     */
    public static Resource getResourceValue(Resource subject, Property prop) {
        StmtIterator ni = subject.listProperties(prop);
        while (ni.hasNext()) {
            RDFNode n = ni.next().getObject();
            if (n instanceof Resource) {
                ni.close();
                return (Resource)n;
            }
        }
        return null;
    }
    
    /**
     * Find a name to give a resource
     */
    public static String getNameFor(Resource r) {
        String label = getStringValue(r, RDFS.label);
        if (label != null) return label;
        if (r.isURIResource()) {
            String uri = r.getURI();
            int split = uri.lastIndexOf('#');
            if (split == -1) split = uri.lastIndexOf('/');
            return uri.substring(split + 1);
        } else {
            return r.getId().toString();
        }
    }
    
    /**
     * Find all matches for the given regex within a string.
     * OK this is not an *RDF* util, so sue me.
     */
    public static List<String> allMatches(Pattern regex, String target) {
        List<String> result = new ArrayList<String>();
        Matcher m = regex.matcher(target);
        int start = 0;
        while (m.find(start)) {
            result.add( m.group());
            start = m.end();
        }
        return result;
    }

    /**
        Answer the integer value of property p on resource x, or
        ifAbsent if there isn't one.
    */
	public static int getIntValue(Resource x, Property p, int ifAbsent) {
		Statement s = x.getProperty( p );
		return s == null ? ifAbsent : s.getInt();
	}

	/**
	    Return the boolean value of property <code>p</code> on resource
	    <code>r</code>. If there is no p-value, or the p-value is not a
	    literal, return <code>ifAbsent</code>. Otherwise return true if
	    the literal has spelling "true" or "yes", false if it has the
	    spelling "false" or "no", and an unspecified value otherwise.
	*/
	public static boolean getBooleanValue( Resource r, Property p, boolean ifAbsent ) {
		Statement s = r.getProperty( p );
		if (s == null) return ifAbsent;
		RDFNode o = s.getObject();
		if (o.isLiteral()) {
			Literal ol = (Literal) o;
			String sp = ol.getLexicalForm();
			return sp.equalsIgnoreCase("yes") || sp.equalsIgnoreCase("true");
		}
		return ifAbsent;
	}

	/**
	    Return an optional boolean value of a property. If there is no p-value,
	    or the p-value isn't a literal with spelling "true", "false", "yes",
	    "no", or "optional", return the ifAbsent value. Otherwise return null
	    for optional, TRUE for true/yes, or FALSE for false/no.
	*/
	public static Boolean getOptionalBooleanValue(Resource x, Property p, Boolean ifAbsent) {
		Statement s = x.getProperty( p );
		if (s == null) return ifAbsent;		
		RDFNode o = s.getObject();		
		if (o.isLiteral()) {
			Literal ol = (Literal) o;
			String sp = ol.getLexicalForm();
			if (sp.equalsIgnoreCase("optional")) return null;
			if (sp.equalsIgnoreCase("yes") || sp.equalsIgnoreCase("true")) return Boolean.TRUE;
			if (sp.equalsIgnoreCase("no") || sp.equalsIgnoreCase("false")) return Boolean.FALSE;
		}
		return ifAbsent;
	}
}

