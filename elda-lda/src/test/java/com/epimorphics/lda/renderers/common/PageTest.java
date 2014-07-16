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
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.*;

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

    @Test
    public void testIsPartOf() {
        assertEquals( ResourceFactory.createResource( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all" ), page.isPartOf() );
    }

    @Test
    public void testDefinition() {
        // TODO test disabled until https://github.com/epimorphics/elda/issues/72 is resolved
        // assertEquals( ResourceFactory.createResource( "http://localhost:8080/standalone/hello/meta/games.vhtml" ), page.definition() );
    }

    @Test
    public void testExtendedMetadataURL() {
        assertEquals( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all", page.extendedMetadataURL() );
    }

    @Test
    public void testPageLinks() {
        assertEquals( ResourceFactory.createResource( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all&_page=0" ),
                page.firstPage() );
        assertEquals( ResourceFactory.createResource( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all&_page=1" ),
                page.nextPage() );
        assertNull( page.prevPage() );
        assertNull( page.lastPage() );
    }

    @Test
    public void testFormats() {
        List<String> formats = new ArrayList<String>();

        for (PageFormat pf: page.formats()) {
            formats.add( pf.mimeType().toString() );
        }

        assertTrue( formats.contains( "text/turtle" ));
        assertTrue( formats.contains( "application/xml" ));
        assertTrue( formats.contains( "application/json" ));
    }

    @Test
    public void testViews() {
        List<String> viewNames = new ArrayList<String>();

        for (EldaView v: page.views()) {
            viewNames.add( v.viewName() );
        }

        assertTrue( viewNames.contains( "basic" ));
        assertTrue( viewNames.contains( "all" ));
        assertTrue( viewNames.contains( "description" ));
    }

    @Test
    public void testTermBindings() {
        List<Binding<Resource>> bindings = page.termBindings();
        boolean found = false;

        for (Binding<Resource> b: bindings) {
            if (b.label().equals( "playTimeMinutes")) {
                assertTrue( b.value().equals( ResourceFactory.createResource( "http://epimorphics.com/public/vocabulary/games.ttl#playTimeMinutes" ) ));
                found = true;
            }
        }

        assertTrue( found );
    }

    @Test
    public void testVarBindings() {
        List<Binding<String>> bindings = page.varBindings();
        boolean found = false;

        for (Binding<String> b: bindings) {
            if (b.label().equals( "_suffix")) {
                assertTrue( b.value().equals( "vhtml" ));
                found = true;
            }
        }

        assertTrue( found );
    }

    @Test
    public void testVarBindingsMap() {
        Map<String,String> bindings = page.varBindingsMap();

        assertEquals( "vhtml", bindings.get( "_suffix" ));
    }

    @Test
    public void testEldaLabel() {
        assertEquals( "Elda 1.2.35-SNAPSHOT", page.eldaLabel() );
    }

    @Test
    public void testEldaVersion() {
        assertEquals( "1.2.35-SNAPSHOT", page.eldaVersion() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

