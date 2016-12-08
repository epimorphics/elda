/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.io.StringWriter;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.atlas.lib.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.log.ELog;
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

    /**
     * Construct a new property path by adding a segment to the given path
     */
    protected PropertyPath( PropertyPath parent, String shortName, Property uri ) {
        this.segments = new ArrayList<String>( parent.segments.size() + 1 );
        this.properties = new ArrayList<Property>( parent.segments.size() + 1 );

        this.segments.addAll( parent.segments );
        this.properties.addAll( parent.properties );

        this.segments.add( shortName );
        this.properties.add( uri );
    }
    
    /**
     * Internal use: construct a path from existing property and segment lists
     */
    protected PropertyPath( List<String> segments, List<Property> properties ) {
        this.segments = segments;
        this.properties = properties;
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
     * @return A string denoting this path in a form suitable for HTML presentation
     */
    public String toHTMLString() {
        StringWriter buf = new StringWriter();
        boolean first = true;

        for (String segment: segments) {
            buf.append( first ? "" : " &raquo; " );
            buf.append( "<code class='rdf-path-segment'>" );
            buf.append( tokeniseWords( segment ) );
            buf.append( "</code>" );
            first = false;
        }

        return buf.toString();
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

    /**
     * Return a new property path with a new segment at the end
     *
     * @param shortName The short name for the new segment, or null
     * @param uri The URI for the new segment, or null
     * @param snr The short name service, which may be null if both shortName
     * and uri are non-null
     * @return A new property path corresponding to this path with the segment
     * <code>.&lt;shortName&gt;</code> appended
     */
    public PropertyPath append( String shortName, String uri, ShortNameRenderer snr ) {
        if (shortName == null) {
            shortName = snr.shorten( uri );
        }

        if (uri == null) {
            uri = snr.expand( shortName );
        }

        return new PropertyPath( this, shortName, ResourceFactory.createProperty( uri ) );
    }

    /** @return The last link along this path */
    public Property terminal() {
        return properties.isEmpty() ? null : properties.get( properties.size() -1 );
    }
    
    /** @return A new path in which the first segment of this path has been removed */
    public PropertyPath shift() {
        if (properties.size() == 0 && segments.size() == 0) {
            throw new RuntimeException( "Tried to shift() an empty PropertyPath" );
        }
        
        List<String> shiftedSegments = new ArrayList<String>();
        List<Property> shiftedProps = new ArrayList<Property>();
        
        for (int i = 1; i < properties.size(); i++) {
            shiftedProps.add( properties.get( i ));
        }
        for (int i = 1; i < segments.size(); i++) {
            shiftedSegments.add( segments.get( i ));
        }
        
        return new PropertyPath( shiftedSegments, shiftedProps );
    }

    /** @return True if this is the empty path */
    public boolean isEmpty() {
        return properties.isEmpty() && segments.isEmpty();
    }
    
    
    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Segment the path by the '.' character, but don't allow null paths
     * @param path A path string to be segmented
     * @return An array of the components of the path
     * @throws IllegalArgumentException if <code>path</code> is null
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
                        log.warn(ELog.message( "[%s]: warning: property path uses short name '%s' which does not have an expansion to a URI", segment));
                        properties.add( STAR );
                    }
                    else {
                        properties.add( ResourceFactory.createProperty( uri ));
                    }
                }
            }
        }
    }

    /**
     * Tokenise the input string into words based on camelCase boundaries and hyphen characters.
     * If the input string is already tokenised into words (determined by whether it contains a
     * space character or not), return the string unchanged.
     * @param name The input name to tokenise
     * @return The input string, with camel-case boundaries, hyphens and underscores replaced by spaces.
     */
    protected String tokeniseWords( String name ) {
        if (name.matches( "[^\\p{Space}]*(((\\p{Lower}|\\p{Digit})(\\p{Upper}))|[-_])[^\\p{Space}]*" )) {
            String deCamelCased = name.replaceAll( "(\\p{Lower}|\\p{Digit})(\\p{Upper})", "$1-$2" );
            List<String> correctlyCased = new ArrayList<String>();

            for (String word: deCamelCased.split( "[-_]" )) {
                if (word.length() > 0) {
                    correctlyCased.add( word.substring( 0, 1 ).toLowerCase() + word.substring( 1) );
                }
            }

            return StrUtils.strjoin( " ", correctlyCased );
        }
        else {
            return name;
        }
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

