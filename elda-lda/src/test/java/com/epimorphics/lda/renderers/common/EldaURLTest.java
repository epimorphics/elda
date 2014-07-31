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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for {@link EldaURL}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class EldaURLTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( EldaURLTest.class );

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
    public void testEldaURLString() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?_abc=def" );
        assertNotNull( eu );
    }

    @Test
    public void testEldaURLURI() throws URISyntaxException {
        URI u = new URI( "http://foo.bar.com/fubar?_abc=def" );
        EldaURL eu = new EldaURL( u );
        assertNotNull( eu );
    }

    @Test
    public void testSetParameter0() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar" );
        assertEquals( "http://foo.bar.com/fubar?a=b", eu.withParameter( EldaURL.OPERATION.SET, "a", "b" ).toString() );
    }

    @Test
    public void testSetParameter1() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=c" );
        assertEquals( "http://foo.bar.com/fubar?a=b", eu.withParameter( EldaURL.OPERATION.SET, "a", "b" ).toString() );
    }

    @Test
    public void testSetParameter2() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?b=c" );
        String newEu = eu.withParameter( EldaURL.OPERATION.SET, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=b&b=c".equals( newEu ) ||
                    "http://foo.bar.com/fubar?b=c&a=b".equals( newEu ));
    }

    @Test
    public void testAddParameter0() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar" );
        String newEu = eu.withParameter( EldaURL.OPERATION.ADD, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=b".equals( newEu ) );
    }

    @Test
    public void testAddParameter1() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=b" );
        String newEu = eu.withParameter( EldaURL.OPERATION.ADD, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=b".equals( newEu ) );
    }

    @Test
    public void testAddParameter2() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=c" );
        String newEu = eu.withParameter( EldaURL.OPERATION.ADD, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=c,b".equals( newEu ) );
    }

    @Test
    public void testAddParameter3() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=c,d" );
        String newEu = eu.withParameter( EldaURL.OPERATION.ADD, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=c,d,b".equals( newEu ) );
    }

    @Test
    public void testAddParameter4() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=c,d&e=f" );
        String newEu = eu.withParameter( EldaURL.OPERATION.ADD, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=c,d,b&e=f".equals( newEu ) ||
                    "http://foo.bar.com/fubar?e=f&a=c,d,b".equals( newEu ));
    }

    @Test
    public void testRemoveParameter0() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar" );
        String newEu = eu.withParameter( EldaURL.OPERATION.REMOVE, "a", "b" ).toString();
        assertEquals( "http://foo.bar.com/fubar", newEu );
    }

    @Test
    public void testRemoveParameter1() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=b" );
        String newEu = eu.withParameter( EldaURL.OPERATION.REMOVE, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar".equals( newEu ) );
    }

    @Test
    public void testRemoveParameter2() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=b,c" );
        String newEu = eu.withParameter( EldaURL.OPERATION.REMOVE, "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=c".equals( newEu ) );
    }

    @Test
    public void testRemoveParameter3() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=b,c" );
        String newEu = eu.withParameter( EldaURL.OPERATION.REMOVE, "a", "c" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=b".equals( newEu ) );
    }

    @Test
    public void testRemoveParameter4() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=b,c" );
        String newEu = eu.withParameter( EldaURL.OPERATION.REMOVE, "a", "d" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=b,c".equals( newEu ) );
    }

    @Test
    public void testRemoveParameter5() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=c,d&e=f" );
        String newEu = eu.withParameter( EldaURL.OPERATION.REMOVE, "a", "c" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=d&e=f".equals( newEu ) ||
                    "http://foo.bar.com/fubar?e=f&a=d".equals( newEu ));
    }

    public void testSetParameter0Str() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar" );
        assertEquals( "http://foo.bar.com/fubar?a=b", eu.withParameter( "SET", "a", "b" ).toString() );
    }

    @Test
    public void testRemoveParameter1Str() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=b" );
        String newEu = eu.withParameter( "REMOVE", "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar".equals( newEu ) );
    }

    @Test
    public void testAddParameter2Str() {
        EldaURL eu = new EldaURL( "http://foo.bar.com/fubar?a=c" );
        String newEu = eu.withParameter( "ADD", "a", "b" ).toString();
        assertTrue( "http://foo.bar.com/fubar?a=c,b".equals( newEu ) );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

