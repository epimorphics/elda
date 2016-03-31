/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.Transcoding;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A service object that augments the {@link ShortnameService} to add the
 * capability of consistently generating new short names for URIs that do
 * not yet have declared short names.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class ShortNameRenderer
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    private static final Logger log = LoggerFactory.getLogger( ShortNameRenderer.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The wrapped short name service, if any */
    private ShortnameService shortNameService;

    /** Map of strings to URIs forming the locally constructed shortened names */
    private Map<String, String> shortNameToURI = new HashMap<String, String>();

    /** Map of URIs to strings forming the locally constructed shortened names */
    private Map<String, String> uriToShortName = new HashMap<String, String>();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a short name renderer using an existing short name service as a
     * provider of pre-existing short names.
     * @param sns Existing short name service, or null
     */
    public ShortNameRenderer( ShortnameService sns, Iterable<Binding<Resource>> bindings ) {
        shortNameService = sns;
        addBindings( bindings );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Return the wrapped short name service, or throw an exception
     */
    public ShortnameService shortNameService() {
        return shortNameService( true );
    }

    /** Return the wrapped short name service; only throw an exception
     * if <code>required</code> is true.
     * @param required If true, wrapped service must be present
     * @throws EldaException
     */
    public ShortnameService shortNameService( boolean required ) {
        if (required && this.shortNameService == null) {
            throw new EldaException( "Expected wrapped short name service not to be null", null, EldaException.SERVER_ERROR );
        }

        return this.shortNameService;
    }

    /* Delegated to wrapped SNS */

    /**
        Answer true iff the named type has been declared (or is by default)
        to be a datatype (rather than an object type).
    */
    public boolean isDatatype( String type ) {
        return shortNameService().isDatatype( type );
    }

    /**
        If r is a resource, answer r; if it is a literal with lexical form l,
        answer normaliseResource(l); otherwise throw an API exception.
    */
    public Resource asResource( RDFNode r ) {
        return shortNameService().asResource( r );
    }

    /**
        Answer a resource with uri = expand(s). If there's no such expansion
        but s "looks like" a uri, return a resource with uri = s. Otherwise
        throw an API exception.
    */
    public Resource asResource( String s ) {
        return shortNameService().asResource( s );
    }

    /**
     * Return the expansion of <code>shortName</code> to a URI. If
     * <code>shortName</code> is not a recognised short name, it is
     * return unchanged.
     * @param shortName The name to expand
     * @return The expansion of shortName, or shortName unchanged
    */
    public String expand( String shortName ) {
        String uri = lookupShortName( shortName );
        return (uri == null) ? shortName : uri;
    }

    /**
     * Return the shortened name for the given <code>uri</code>. If a short name
     * exists in the wrapped short name service, use that by preference. Otherwise,
     * compress using the usual approach, and remember the shortening locally
     * if <code>memoise</code> is true.
     * @param uri The URI to be shortened
     * @param memoise If true, remember any newly created short form
     * @return The short form of the name
     */
    public String shorten( String uri, boolean memoise ) {
        String shortName = lookupURI( uri );

        if (shortName == null) {
            shortName = shortForm( uri );

            if (!shortName.equals( uri ) && memoise) {
                addShortName( shortForm( uri ), uri );
            }
        }

        return shortName;
    }

    /**
     * Return a short name, memoising by default. See
     * {@link #shorten(String, boolean)}
     */
    public String shorten( String uri ) {
        return shorten( uri, true );
    }

    /**
     * @return True if this renderer has an encapsulated short name service
     */
    public boolean hasShortNameService() {
        return shortNameService( false ) != null;
    }


    /**
     * @return True if the given string is path formed from known shortnames
     */
    public boolean isKnownShortnamePath( String path ) {
        boolean known = true;

        for( String segment: StringUtils.split( path, "." )) {
            if (hasShortNameService()) {
                String uri = shortNameService().expand( segment );
                known = known && (uri != null);
            }
        }

        return known;
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Initialise the local short names map with the given bindings
     * @param bindings An initial list of bindings, e.g. from the page object
     */
    protected void addBindings( Iterable<Binding<Resource>> bindings ) {
        if (bindings != null) {
            for( Binding<Resource> b: bindings) {
                addShortName( b.label(), b.value().getURI() );
            }
        }
    }

    /**
     * Add a single local binding. If already in the map, ignore it
     * @param shortName
     * @param uri
     */
    protected void addShortName( String shortName, String uri ) {
        String existing = lookupShortName( shortName );

        if (existing != null) {
            if (!existing.equals(  uri  )) {
                log.warn(ELog.message("attempt to overwrite existing binding '%s' for short-name '%s' with new value '%s' was ignored", existing, shortName , uri ));
            }
        }
        else {
            storeShortName( shortName, uri );
        }
    }

    /**
     * Look up an existing short name with no side effects. Return null if
     * not found.
     * @param shortName The shortname key to look up
     * @return The current URI for the shortname, or null
     */
    protected String lookupShortName( String shortName ) {
        String uri = null;
        if (hasShortNameService()) {
            uri = shortNameService( false ).asContext().getURIfromName( shortName );
        }

        if (uri == null) {
            uri = shortNameToURI.get( shortName  );
        }

        return uri;
    }

    /**
     * Look to see if we have an existing short name for a given URI, or
     * return null
     * @param uri The URI to contract
     * @return The shortname for the URI, if known, or null
     */
    protected String lookupURI( String uri ) {
        String shortName = null;

        if (hasShortNameService()) {
            shortName = shortNameService( false ).asContext().getNameForURI( uri );
        }

        if (shortName == null) {
            shortName = uriToShortName.get( uri );
        }

        return shortName;
    }

    /**
     * Add a new shortname - URI pair
     */
    protected void storeShortName( String shortName, String uri ) {
        shortNameToURI.put(  shortName,  uri );
        uriToShortName.put( uri, shortName );
    }

    /**
     * Return the short-form of a URI, delegating the work to {@link Transcoding}
     */
    protected String shortForm( String uri ) {
        return Transcoding.encode( prefixMapping(), uri );
    }

    /**
     * Return a prefix mapping, defaulting to the prefix mapping of the
     * wrapped short name service. If no given prefix mapping, return
     * an empty map.
     */
    protected PrefixMapping prefixMapping() {
        return hasShortNameService() ? shortNameService().getPrefixes() : PrefixMapping.Factory.create();
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

