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

import java.util.*;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link EldaView}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class EldaViewTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    static final Model apiMetadataModel = ModelFactory.createDefaultModel();
    static final Model apiObjectModel = ModelFactory.createDefaultModel();
    static final Model apiResultsModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_BWQ );

    static final String view_uri = "http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=salmonella&_metadata=all&_page=0";

    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        // we are forced to use the legacy imposteriser because APIResultSet does not
        // have an interface that it conforms to
        setImposteriser(ClassImposteriser.INSTANCE);

        setThreadingPolicy(new Synchroniser());
    }};

    ResultsModel rm;
    private EldaView view;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void before() {
        rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel ) );
        view = new EldaView( rm.page(), ResourceFactory.createResource( view_uri ) );
    }

    @Test
    public void testLabel() {
        assertEquals( "salmonella", view.label() );
    }

    @Test
    public void testViewName() {
        assertEquals( "salmonella", view.viewName() );
    }

    @Test
    public void testPropertyPaths() {
        List<PropertyPath> paths = view.propertyPaths();
        List<String> pathStrs = new ArrayList<String>();

        for (PropertyPath p: paths) {
            pathStrs.add( p.toString() );
        }

        assertNotNull( paths );
        assertEquals( 13, paths.size() );
        assertTrue( pathStrs.contains( "sampleWeek.label" ) );
    }

    @Test
    public void testIsFormatOf() {
        assertEquals( "http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_view=all&_metadata=all&_page=0&_lang=en,cy", view.isVersionOf().getURI() );
    }

    @Test
    public void testBasicView() {
        EldaView basic = new EldaView.BasicView( rm.page() );

        assertEquals( "basic", basic.label() );
        assertEquals( 2, basic.propertyPaths().size() );
        assertEquals( "rdfs:label", basic.propertyPaths().get( 0 ).toString() );
        assertEquals( "rdf:type", basic.propertyPaths().get( 1 ).toString() );
    }

    @Test
    public void testDescriptionView() {
        EldaView all = new EldaView.DescriptionView( rm.page() );

        assertEquals( "all", all.label() );
        assertEquals( 1, all.propertyPaths().size() );
        assertEquals( "*", all.propertyPaths().get( 0 ).toString() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

