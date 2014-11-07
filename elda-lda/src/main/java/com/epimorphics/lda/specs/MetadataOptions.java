/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.specs;

import java.util.*;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Encapsulates a set of metadata options extracted from the API configuration.
 */
@SuppressWarnings( "serial" )
public class MetadataOptions
extends HashSet<String>
{
    /** List of all metadata option types. TODO: remove this when issue 80 is addressed
     * https://github.com/epimorphics/elda/issues/80
     */
    public static final String[] ALL_OPTIONS = {"versions", "formats", "bindings", "execution"};

    /**
     * Construct a new collection of metadata options from any <code>elda:metadataOptions</code>
     * properties of the given configuration root
     * @param root Non-optional configuration root resource
     * @param defaultOptions Optional string of options to set in the case that <code>root</code> does
     * not specify any metadata options
     */
    public MetadataOptions( Resource root, String defaultOptions ) {
        if (root == null) {
            throw new IllegalArgumentException( "Cannot create metadata options if root resource is null" );
        }

        for (Statement stmt: root.listProperties( ELDA_API.metadataOptions ).toList()) {
            addOptions( stmt.getString() );
        }

        if (!root.hasProperty( ELDA_API.metadataOptions ) && defaultOptions != null) {
            addOptions( defaultOptions );
        }
    }

    /**
     * Constructor that collaborates with the #all method
     */
    protected MetadataOptions( String[] options ) {
        for (String option: options) {
            addOptions( option );
        }
    }

    /** @deprecated See constructor instead */
    @Deprecated
    public static String[] get( Resource R ) {
        throw new RuntimeException( "Deprecated method MetadataOptions.get() should not be called" );
    }

    /**
     * @return This set of metadata options as an array of strings.
     */
    public String[] asArray() {
        return toArray( new String[this.size()] );
    }

    /**
     * Add a string containing comma-separated option strings to this collection of metadata options
     */
    protected void addOptions( String options ) {
        for (String opt: options.split( " *, *" )) {
            add( opt.toLowerCase() );
        }
    }

    /**
     * The Zen method: make me one with everything!
     * @return A collection of all metadata options
     */
    public static MetadataOptions allOptions() {
        return new MetadataOptions( ALL_OPTIONS );
    }
}
