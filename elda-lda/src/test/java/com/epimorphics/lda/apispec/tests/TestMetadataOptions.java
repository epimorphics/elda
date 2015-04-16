/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.apispec.tests;


import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link MetadataOptions}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class TestMetadataOptions
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( TestMetadataOptions.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Test
    public void testMetadataOptions() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource( "http://example/r" );

        MetadataOptions md0 = new MetadataOptions( r, "foo,bar" );
        assertTrue( md0.contains( "foo" ));
        assertTrue( md0.contains( "bar" ));
        assertFalse( md0.contains( "bindings") );
        assertFalse( md0.contains( "formats") );

        r.addProperty( ELDA_API.metadataOptions, "formats,bindings" );

        MetadataOptions md1 = new MetadataOptions( r, "foo" );
        assertFalse( md1.contains( "foo" ));
        assertFalse( md1.contains( "bar" ));
        assertTrue( md1.contains( "bindings") );
        assertTrue( md1.contains( "formats") );
    }

    @Test
    public void testAsArray() {
        Model m = ModelFactory.createDefaultModel();
        Resource r = m.createResource( "http://example/r" );

        MetadataOptions md0 = new MetadataOptions( r, "foo,bar,foo" );

        String[] a = md0.asArray();
        assertEquals( 2, a.length );
        assertTrue( (a[0].equals( "foo" ) && a[1].equals( "bar" )) ||
                    (a[1].equals( "foo" ) && a[0].equals( "bar" )) );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

