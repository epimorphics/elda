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
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link PageFormat}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PageFormatTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    static final Model apiMetadataModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_METADATA_GAMES );
    static final Model apiObjectModel = ModelIOUtils.modelFromTurtle( Fixtures.PAGE_OBJECT_GAMES );
    static final Model apiResultsModel = ModelFactory.createUnion( apiMetadataModel, apiObjectModel );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        // we are forced to use the legacy imposteriser because APIResultSet does not
        // have an interface that it conforms to
        setImposteriser(ClassImposteriser.INSTANCE);

        setThreadingPolicy(new Synchroniser());
    }};

    private PageFormat pageFormat;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void before() {
        ResultsModel rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel ) );
        pageFormat = new PageFormat( rm.page(), ResourceFactory.createResource( "http://localhost:8080/standalone/hello/games.ttl?_metadata=all" ) );
    }

    @Test
    public void testPageFormat() {
        assertNotNull( pageFormat );
    }

    @Test
    public void testLabel() {
        assertEquals( "ttl", pageFormat.label() );
    }

    @Test
    public void testMimeType() {
        assertEquals( "text/turtle", pageFormat.mimeType().toString() );
    }

    @Test
    public void testPage() {
        assertNotNull( pageFormat.page() );
    }

    @Test
    public void testIsFormatOf() {
        assertEquals( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all", pageFormat.isFormatOf().getURI() );
    }

    @Test
    public void testURI() {
        assertEquals( "http://localhost:8080/standalone/hello/games.ttl?_metadata=all", pageFormat.getURI() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

