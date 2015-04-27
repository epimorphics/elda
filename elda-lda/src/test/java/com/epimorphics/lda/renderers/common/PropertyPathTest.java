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

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Unit test for {@link PropertyPath}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PropertyPathTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery();

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
    public void testPropertyPathString() {
        assertEquals( "foo", new PropertyPath( "foo" ).toString() );
        assertEquals( "foo.bar", new PropertyPath( "foo.bar" ).toString() );
        assertEquals( "foo.bar.fubar", new PropertyPath( "foo.bar.fubar" ).toString() );
    }

    @Test
    public void testPropertyPathStringArray() {
        assertEquals( "foo", new PropertyPath( new String[] {"foo"} ).toString() );
        assertEquals( "foo.bar", new PropertyPath( new String[] {"foo", "bar"} ).toString() );
        assertEquals( "foo.bar.fubar", new PropertyPath( new String[] {"foo", "bar", "fubar"} ).toString() );
    }

    @Test
    public void testNullProperatyPath() {
        assertEquals( "", new PropertyPath().toString() );
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testNullStringPath() {
        new PropertyPath( (String[]) null );
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testNullArrayPath() {
        new PropertyPath( (String) null );
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testEmptyPath() {
        new PropertyPath( "" );
    }

    @Test
    public void testSize() {
        assertEquals( 1, new PropertyPath( "foo" ).size() );
        assertEquals( 3, new PropertyPath( "foo.bar.fubar" ).size() );
    }

    @Test
    public void testPathBeginsWith() {
        ShortnameService sns = Fixtures.shortNameServiceFixture();
        ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        PropertyPath path = new PropertyPath( "name_p" );

        assertTrue( path.beginsWith( ResourceFactory.createProperty( "http://example/test/p" ), snr ) );
        assertFalse( path.beginsWith( ResourceFactory.createProperty( "http://example/test/q" ), snr ) );

        path = new PropertyPath( "name_p.name_q" );

        assertTrue( path.beginsWith( ResourceFactory.createProperty( "http://example/test/p" ), snr ) );
        assertFalse( path.beginsWith( ResourceFactory.createProperty( "http://example/test/q" ), snr ) );
    }

    @Test
    public void testWildcardPathBeginsWith() {
        ShortnameService sns = Fixtures.shortNameServiceFixture();
        ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        PropertyPath path = new PropertyPath( "*" );

        assertTrue( path.beginsWith( ResourceFactory.createProperty( "http://example/test/p" ), snr ) );
        assertTrue( path.beginsWith( ResourceFactory.createProperty( "http://example/test/q" ), snr ) );

        path = new PropertyPath( "*.foo" );

        assertTrue( path.beginsWith( ResourceFactory.createProperty( "http://example/test/p" ), snr ) );
        assertTrue( path.beginsWith( ResourceFactory.createProperty( "http://example/test/q" ), snr ) );
    }

    @Test
    public void testEmptyPathBeginsWith() {
        ShortnameService sns = Fixtures.shortNameServiceFixture();
        ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        PropertyPath path = new PropertyPath();

        assertFalse( path.beginsWith( ResourceFactory.createProperty( "http://example/test/p" ), snr ) );
        assertFalse( path.beginsWith( ResourceFactory.createProperty( "http://example/test/q" ), snr ) );
    }

    @Test
    public void testWith() {
        ShortnameService sns = Fixtures.shortNameServiceFixture();
        ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        PropertyPath path = new PropertyPath( "a.b" );

        assertEquals( "a.b.name_p", path.append( "name_p", "http://example/test/p", null ).toString() );
        assertEquals( "a.b.name_p", path.append( "name_p", null, snr ).toString() );
        assertEquals( "a.b.name_p", path.append( null, "http://example/test/p", snr ).toString() );
    }

    @Test
    public void testTerminal() {
        PropertyPath path = new PropertyPath();

        assertNull( path.terminal() );

        PropertyPath path1 = path.append( "name_p", "http://example/test/p", null );
        PropertyPath path2 = path1.append( "name_q", "http://example/test/q", null );

        assertEquals( "http://example/test/p", path1.terminal().getURI() );
        assertEquals( "http://example/test/q", path2.terminal().getURI() );
    }

    @Test
    public void testShift0() {
        PropertyPath p = new PropertyPath( "foo.bar.bam" );
        
        PropertyPath s = p.shift();
        assertEquals( "bar.bam", s.toString() );
        
        s = s.shift();
        assertEquals( "bam", s.toString() );
        
        s = s.shift();
        assertEquals( "", s.toString() );
    }
    
    @Test
    public void testIsEmpty() {
        assertTrue( new PropertyPath().isEmpty() );
        assertFalse( new PropertyPath( "foo" ).isEmpty() );
    }
    
    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

