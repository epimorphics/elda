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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Unit tests for {@link ShortNameRenderer}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class ShortNameRendererTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( ShortNameRendererTest.class );


    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /** Test that isDatatype is delegated correctly */
    @Test
    public void testIsDatatype() {
        final ShortnameService sns = context.mock( ShortnameService.class );
        final ShortNameRenderer snr = new ShortNameRenderer( sns );

        context.checking(new Expectations() {{
            oneOf (sns).isDatatype( "foo" );
        }});

        snr.isDatatype( "foo" );
    }

    /** Test that asResource( RDFNode) is delegated correctly */
    @Test
    public void testAsResourceRDFNode() {
        final ShortnameService sns = context.mock( ShortnameService.class );
        final ShortNameRenderer snr = new ShortNameRenderer( sns );

        final RDFNode n = ResourceFactory.createResource( "http//example/test" );

        context.checking(new Expectations() {{
            oneOf (sns).asResource( n );
        }});

        snr.asResource( n );
    }

    /** Test that asResource( String ) is delegated correctly */
    @Test
    public void testAsResourceString() {
        final ShortnameService sns = context.mock( ShortnameService.class );
        final ShortNameRenderer snr = new ShortNameRenderer( sns );

        context.checking(new Expectations() {{
            oneOf (sns).asResource( "foo" );
        }});

        snr.asResource( "foo" );
    }

    /** Tests on getting the wrapped short name service */
    @Test
    public void testGetShortNameService() {
        final ShortnameService sns = context.mock( ShortnameService.class );

        final ShortNameRenderer snr = new ShortNameRenderer( sns );
        assertSame( sns, snr.shortNameService() );
        assertSame( sns, snr.shortNameService( true ) );
        assertSame( sns, snr.shortNameService( false ) );

        final ShortNameRenderer snrNoService = new ShortNameRenderer( null );
        assertNull( snrNoService.shortNameService( false ) );
    }

    @Test(expected=EldaException.class)
    public void testGetShortNameServiceRaise() {
        final ShortNameRenderer snrNoService = new ShortNameRenderer( null );
        snrNoService.shortNameService();
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

