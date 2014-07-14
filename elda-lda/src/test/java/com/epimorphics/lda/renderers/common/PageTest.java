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
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Unit tests for {@link Page}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PageTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( PageTest.class );

    static final Model apiMetadataModel = ModelIOUtils.modelFromTurtle( Fixtures.PAGE_METADATA_GAMES );
    static final Model apiObjectModel = ModelIOUtils.modelFromTurtle( Fixtures.PAGE_OBJECT_GAMES );
    static final Model apiResultsModel = ModelFactory.createUnion( apiMetadataModel, apiObjectModel );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        // we are forced to use the legacy imposteriser because APIResultSet does not
        // have an interface that it conforms to
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    private Page page;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void before() {
        ResultsModel rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel ) );
        page = rm.page();
    }

    @Test
    public void testPage() {
        assertNotNull( page );
    }

    @Test
    public void testIsSingleItem() {
        assertFalse( page.isItemEndpoint() );
    }

    @Test
    public void testPageNumber() {
        assertEquals( 0, page.pageNumber() );
    }

    @Test
    public void testItemsPerPage() {
        assertEquals( 10, page.itemsPerPage() );
    }

    @Test
    public void testStartIndex() {
        assertEquals( 1, page.startIndex() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

