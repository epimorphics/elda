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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.hp.hpl.jena.rdf.model.*;

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
        final ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        context.checking(new Expectations() {{
            oneOf (sns).isDatatype( "foo" );
        }});

        snr.isDatatype( "foo" );
    }

    /** Test that asResource( RDFNode) is delegated correctly */
    @Test
    public void testAsResourceRDFNode() {
        final ShortnameService sns = context.mock( ShortnameService.class );
        final ShortNameRenderer snr = new ShortNameRenderer( sns, null );

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
        final ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        context.checking(new Expectations() {{
            oneOf (sns).asResource( "foo" );
        }});

        snr.asResource( "foo" );
    }

    /** Tests on getting the wrapped short name service */
    @Test
    public void testGetShortNameService() {
        final ShortnameService sns = context.mock( ShortnameService.class );

        final ShortNameRenderer snr = new ShortNameRenderer( sns, null );
        assertSame( sns, snr.shortNameService() );
        assertSame( sns, snr.shortNameService( true ) );
        assertSame( sns, snr.shortNameService( false ) );

        final ShortNameRenderer snrNoService = new ShortNameRenderer( null, null );
        assertNull( snrNoService.shortNameService( false ) );
    }

    @Test(expected=EldaException.class)
    public void testGetShortNameServiceRaise() {
        final ShortNameRenderer snrNoService = new ShortNameRenderer( null, null );
        snrNoService.shortNameService();
    }

    @Test
    public void testExpandInEmptyMap() {
        final ShortNameRenderer snr = new ShortNameRenderer( null, null );
        assertEquals( "foo", snr.expand( "foo" ) );
    }

    @Test
    public void testExpandWithBindings() {
        List<Binding<Resource>> bindings = new ArrayList<Binding<Resource>>();
        bindings.add( new Binding<Resource>( "foo1", ResourceFactory.createResource( "http://example/foo1" )) );
        bindings.add( new Binding<Resource>( "foo2", ResourceFactory.createResource( "http://example/foo2" )) );

        final ShortNameRenderer snr = new ShortNameRenderer( null, bindings );

        assertEquals( "http://example/foo1", snr.expand( "foo1" ) );
        assertEquals( "http://example/foo2", snr.expand( "foo2" ) );
        assertEquals( "foo3", snr.expand( "foo3" ) );
    }

    @Test
    public void testExpandWithService() {
        ShortnameService sns = stubShortNameService();
        ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        assertEquals( "http://example/test/p", snr.expand( "name_p" ));
    }

    @Test
    public void testShorten1() {
        ShortnameService sns = stubShortNameService();
        ShortNameRenderer snr = new ShortNameRenderer( sns, null );

        assertEquals( "name_p", snr.shorten( "http://example/test/p" ));
        assertEquals( "_foo", snr.shorten( "http://example/test/foo" ));
    }

    @Test
    public void testShorten2() {
        List<Binding<Resource>> bindings = new ArrayList<Binding<Resource>>();
        bindings.add( new Binding<Resource>( "foo1", ResourceFactory.createResource( "http://example/foo1" )) );

        final ShortNameRenderer snr = new ShortNameRenderer( null, bindings );

        assertEquals( "foo1", snr.shorten( "http://example/foo1" ) );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    static final Model apiModel1 = ModelIOUtils.modelFromTurtle
            ( "@prefix : <http://example/test/>. "
            + "<stub:root> a api:API. "
            + ":p a rdf:Property; api:label 'name_p'. "
            + ":q a rdf:Property; api:label 'name_q'; rdfs:range xsd:decimal."
            );

    private ShortnameService stubShortNameService() {
        Resource root = apiModel1.createResource( "stub:root" );
        return new StandardShortnameService( root, apiModel1, null );
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

