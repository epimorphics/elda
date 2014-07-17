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

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( PropertyPath.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The individual segments of the path */
    private List<String> segments;

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
    }

    /**
     * Construct the empty path. This is the only sanctioned way to create an
     * empty path - passing null or empty string into other constructors is not
     * allowed.
     */
    public PropertyPath() {
        this.segments = new ArrayList<String>();
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

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

