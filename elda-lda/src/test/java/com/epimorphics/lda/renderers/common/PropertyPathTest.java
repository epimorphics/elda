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

import org.junit.Test;

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

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

