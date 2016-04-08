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
import com.epimorphics.lda.vocabularies.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

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


    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        // we are forced to use the legacy imposteriser because APIResultSet does not
        // have an interface that it conforms to
        setImposteriser(ClassImposteriser.INSTANCE);

        setThreadingPolicy(new Synchroniser());
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
        final Model apiMetadataModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_METADATA_GAMES );
        final Model apiObjectModel = ModelIOUtils.modelFromTurtle( Fixtures.PAGE_OBJECT_GAMES );
        final Model apiResultsModel = ModelFactory.createUnion( apiMetadataModel, apiObjectModel );

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
    public void testHasPageData() {
        assertTrue( page.hasPageData() );

        Resource pr = page.asResource();
        page.getModelW().getModel().removeAll( pr, API.page, null );
        assertTrue( page.hasPageData() );

        page.getModelW().getModel().removeAll( pr, OpenSearch.itemsPerPage, null );
        assertTrue( page.hasPageData() );

        page.getModelW().getModel().removeAll( pr, XHV.first, null );
        assertTrue( page.hasPageData() );

        page.getModelW().getModel().removeAll( pr, ResourceFactory.createProperty( XHV.ns + "last" ), null );
        assertTrue( page.hasPageData() );

        page.getModelW().getModel().removeAll( pr, XHV.prev, null );
        assertTrue( page.hasPageData() );

        page.getModelW().getModel().removeAll( pr, XHV.next, null );
        assertFalse( page.hasPageData() );
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
    public void testItems() {
        List<DisplayRdfNode> items = page.items();

        assertEquals( 10, items.size() );
        assertEquals( "http://www.ragnarbrothers.co.uk/html/brief_history_of_the_world1.html", items.get( 0 ).getURI() );
    }

    @Test
    public void testCurrentView() {
        assertEquals( "basic", page.currentView().label() );
    }

    @Test
    public void testFindViewByName() {
        assertEquals( "all", page.findViewByName( "all" ).label() );
    }

    @Test
    public void testListPropertyPaths() {
        List<PropertyPath> paths = page.currentPropertyPaths();

        assertEquals( 2, paths.size() );

        List<String> pathNames = new ArrayList<String>();
        for (PropertyPath p: paths) {
            pathNames.add( p.toString() );
        }
        Collections.sort( pathNames ); // a consistent order makes testing easy

        assertEquals( "label", pathNames.get( 0 ) );
        assertEquals( "type", pathNames.get( 1 ) );
    }

    @Test
    public void testItemHasAllProperties1() {
        assertTrue( page.itemHasAllProperties( "rdfs:label", "http://epimorphics.com/public/vocabulary/games.ttl#players", RDF.type ));
        assertFalse( page.itemHasAllProperties( "rdfs:label", "http://epimorphics.com/public/vocabulary/games.ttl#players", RDF.first ));
    }

    @Test
    public void testItemHasAllProperties2() {
        List<Object> properties = new ArrayList<Object>();
        properties.add( "rdfs:label" );
        properties.add( "http://epimorphics.com/public/vocabulary/games.ttl#players" );
        properties.add( RDF.type );
        assertTrue( page.itemHasAllProperties( properties ));

        properties.add( RDF.first );
        assertFalse( page.itemHasAllProperties( properties ));
    }

    @Test
    public void testResourcesWithAllProperties() {
        List<Object> properties = new ArrayList<Object>();

        List<DisplayRdfNode> rs = page.resourcesWithAllProperties( properties );
        assertEquals( 0, rs.size() );

        properties.add( "rdfs:label" );
        rs = page.resourcesWithAllProperties( properties );
        assertEquals( 31, rs.size() );

        properties.add( "http://epimorphics.com/public/vocabulary/games.ttl#players" );
        rs = page.resourcesWithAllProperties( properties );
        assertEquals( 10, rs.size() );

        properties.add( RDF.type );
        rs = page.resourcesWithAllProperties( properties );
        assertEquals( 10, rs.size() );

        properties.add( RDF.first );
        rs = page.resourcesWithAllProperties( properties );
        assertEquals( 0, rs.size() );
    }
    
    @Test
    public void testResultsCountSummaryNoCount() {
        assertNull( page.resultsCountSummary() );
    }
    
    @Test
    public void testResultsCountSummaryNonEmpty() {
        final Model apiMetadataModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_METADATA_GAMES );
        Resource pageRoot = apiMetadataModel.getResource( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all" );
        pageRoot.addProperty( OpenSearch.totalResults, apiMetadataModel.createTypedLiteral( 99 ) );
        
        final Model apiObjectModel = ModelIOUtils.modelFromTurtle( Fixtures.PAGE_OBJECT_GAMES );
        final Model apiResultsModel = ModelFactory.createUnion( apiMetadataModel, apiObjectModel );

        ResultsModel rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel, "APIResultsModel2" ) );
        Page page1 = rm.page();
        
        assertEquals( "Showing items 1 to 10 of 99", page1.resultsCountSummary() );
    }

    
    @Test
    public void testResultsCountSummaryEmpty() {
        final Model apiMetadataModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_METADATA_GAMES );
        Resource pageRoot = apiMetadataModel.getResource( "http://localhost:8080/standalone/hello/games.vhtml?_metadata=all" );
        pageRoot.addProperty( OpenSearch.totalResults, apiMetadataModel.createTypedLiteral( 0 ) );
        
        final Model apiObjectModel = ModelIOUtils.modelFromTurtle( Fixtures.PAGE_OBJECT_GAMES );
        final Model apiResultsModel = ModelFactory.createUnion( apiMetadataModel, apiObjectModel );

        ResultsModel rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel, "APIResultsModel3" ) );
        Page page1 = rm.page();
        
        assertEquals( "0 results", page1.resultsCountSummary() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

