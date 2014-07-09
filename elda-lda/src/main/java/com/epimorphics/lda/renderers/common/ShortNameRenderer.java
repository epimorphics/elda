/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

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

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( ShortNameRenderer.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The wrapped short name service, if any */
    private ShortnameService shortNameService;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a short name renderer using an existing short name service as a
     * provider of pre-existing short names.
     * @param sns Existing short name service, or null
     */
    public ShortNameRenderer( ShortnameService sns ) {
        shortNameService = sns;
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
        Answer the full name (URI) corresponding to the short name s.
    */
    public String expand( String s ) {
        return null;
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

