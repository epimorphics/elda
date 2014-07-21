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

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * A basic encapsulation of LDA property paths as represented in the
 * metadata for a page.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PropertyPath
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** A fake property to denote the star operator in a property path */
    public static final Property STAR = ResourceFactory.createProperty( ELDA_API.NS + "__STAR" );

    /***********************************/
    /* Static variables                */
    /***********************************/

    private static final Logger log = LoggerFactory.getLogger( PropertyPath.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The individual segments of the path */
    private List<String> segments;

    /** The predicate names that correspond to the shortnames on the path */
    private List<Property> properties;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new path from an encoded path string.
     * @param path A path encoded in <code>shortName[.shortName]*</code> form
     */
    public PropertyPath( String path ) {
        this( segment( path ) );
    }

    /**
     * Construct a new path from an array of the <code>shortName</code> segments
     * @param segments The segments of the path as an array
     * @throws IllegalArgumentException if the segments array is null or empty
     */
    public PropertyPath( String[] segments ) {
        if (segments == null) {
            throw new IllegalArgumentException( "Cannnot create a PropertyPath with a null path" );
        }
        else if (segments.length == 0 || (segments.length == 1 && segments[0].isEmpty())) {
            throw new IllegalArgumentException( "Cannot create a PropertyPath with an empty path" );
        }

        this.segments = Arrays.asList( segments );
        this.properties = new ArrayList<Property>( segments.length );
    }

    /**
     * Construct the empty path. This is the only sanctioned way to create an
     * empty path - passing null or empty string into other constructors is not
     * allowed.
     */
    public PropertyPath() {
        this.segments = new ArrayList<String>();
        this.properties = new ArrayList<Property>( 0 );
    }


    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * @return A string denoting the path in infix form, with dot separators.
     */
    @Override
    public String toString() {
        return StringUtils.join( segments, "." );
    }

    /**
     * @return The number of segments in the path
     */
    public int size() {
        return segments.size();
    }

    /**
     * Return true if the given property matches the first segment in the path -
     * either because the path's shortname expands to the property URI, or because
     * the first segment of the path is <code>*</code>.
     * @param p A property to test
     * @param snr Short name renderer for converting path shortnames to predicate URIs
     * @return True if the given property matches the start of the path
     */
    public boolean beginsWith( Property p, ShortNameRenderer snr ) {
        checkExpanded( snr );
        return !properties.isEmpty() &&
               (properties.get( 0 ).equals( p ) ||
                properties.get( 0 ).equals( STAR )
               );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Segment the path by the '.' character, but don't allow null paths
     * @param path
     * @return
     */
    private static String[] segment( String path ) {
        if (path == null) {
            throw new IllegalArgumentException( "Cannnot create a PropertyPath with a null path" );
        }
        return path.split( "\\." );
    }

    /**
     * Check that the shortnames on the path have been expanded into property URIs
     */
    private void checkExpanded( ShortNameRenderer snr ) {
        if (segments.size() > properties.size()) {
            for (String segment: segments) {
                if (segment.equals( "*" )) {
                    properties.add( STAR );
                }
                else {
                    String uri = snr.expand( segment );
                    if (uri == null) {
                        log.warn( "Warning: property path uses short name '" + segment + "' which does not have an expansion to a URI"  );
                        properties.add( STAR );
                    }
                    else {
                        properties.add( ResourceFactory.createProperty( uri ));
                    }
                }
            }
        }
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

