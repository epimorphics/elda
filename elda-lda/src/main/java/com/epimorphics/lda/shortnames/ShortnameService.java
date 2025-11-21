/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.shortnames;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import java.util.Map;

/**
 * <p>
 * A ShortnameService maps shortnames to full URIs so that classes and
 * properties in web APIs can refer to them compactly and legally. A
 * shortname should always be both a legal NCName and a legal Javascript
 * variable name.
 * </p>
 *
 * <p>
 * The shortname to URI mapping is unique but the URI to shortname
 * is not, there is a preferred shortname for each URI but can also
 * have non-preferred shortnames (e.g. with prefixes).
 * </p>
 *
 * <p>
 * The shortnames are defined via annotations (api:label, rdfs:label)
 * in either the API specification or a referenced vocabulary file.
 * Where such labels are not available we fall back on local names,
 * or prefixed-local names.
 * </p>
 *
 * <p>
 * Derived from the original ShortnameService (which is now an implementation
 * class, StandardShortnameService).
 * </p>
 *
 * @author chris
 */
public interface ShortnameService {

    /**
     * Answer a context object suitable for driving a JSON encoding. Never
     * answers null, but may answer an empty context. Typically the
     * context was supplied when the ShortnameService object was
     * constructed.
     */
    public Context asContext();

    /**
     * Answer a freshly-constructed map from URIs to shortnames, based on
     * the shortnames declared to this ShortnameService. All URIs in the
     * Model have shortnames allocated if they don't have them already.
     * The prefixes in the PrefixMapping may be used when constructing these
     * new shortnames.
     */
    public Map<String, String> constructURItoShortnameMap(Model m, PrefixMapping pm);

    /**
     * Answer the property info record for the property with this shortname.
     */
    public ContextPropertyInfo getPropertyByName(String shortName);

    /**
     * Answer true iff the named type has been declared (or is by default)
     * to be a datatype (rather than an object type).
     */
    public boolean isDatatype(String type);

    /**
     * If r is a resource, answer r; if it is a literal with lexical form l,
     * answer normaliseResource(l); otherwise throw an API exception.
     */
    public Resource asResource(RDFNode r);

    /**
     * Answer a resource with uri = expand(s). If there's no such expansion
     * but s "looks like" a uri, return a resource with uri = s. Otherwise
     * throw an API exception.
     */
    public Resource asResource(String s);

    /**
     * Answer the full name (URI) corresponding to the short name s.
     */
    public String expand(String s);

    /**
     * Return the prefix mapping that this shortname service is managing
     *
     * @return The prefix mapping
     */
    public PrefixMapping getPrefixes();
}
