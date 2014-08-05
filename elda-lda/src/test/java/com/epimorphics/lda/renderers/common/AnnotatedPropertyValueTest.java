/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.rdfutil.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link AnnotatedPropertyValue}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class AnnotatedPropertyValueTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( AnnotatedPropertyValueTest.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testAnnotate() {
        String ns = "http://exmample.com/test#";
        Model m = ModelFactory.createDefaultModel();
        Property p = m.createProperty( ns + "p" );
        ModelWrapper mw = new ModelWrapper( m );

        RDFNodeWrapper prop = new RDFNodeWrapper( mw, p );
        PropertyValue pv = new PropertyValue( prop );

        AnnotatedPropertyValue apv = new AnnotatedPropertyValue( pv );

        assertEquals( 0, apv.annotations().size() );
        assertEquals( "", apv.annotationsString() );

        apv.annotate( "foo" );
        assertEquals( 1, apv.annotations().size() );
        assertEquals( "foo", apv.annotationsString() );

        apv.annotate( "bar" );
        assertEquals( 2, apv.annotations().size() );
        assertEquals( "foo bar", apv.annotationsString() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

