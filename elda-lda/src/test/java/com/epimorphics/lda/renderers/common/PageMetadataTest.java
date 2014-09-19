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
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Unit tests for {@link PageMetadata}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PageMetadataTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    private static final String TEST_ROOT_URI = "http://localhost:8080/standalone/bwq/doc/bathing-water?_metadata=all";

    /***********************************/
    /* Static variables                */
    /***********************************/

    static final Model pageMetadataModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_METADATA_BWQ );

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
    Page page;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void setUp() throws Exception {
        rm = new ResultsModel( Fixtures.mockResultSet( context, pageMetadataModel, ModelFactory.createDefaultModel(), pageMetadataModel ) );
        page = rm.page();
    }

    @Test
    public void testPageRoot() {
        assertEquals( TEST_ROOT_URI, page.metadata().pageRoot().getURI() );
    }

    @Test
    public void testExecution() {
        RDFNodeWrapper exec = page.metadata().execution();
        assertNotNull( exec );
        assertTrue( exec.isAnon() );
    }

    @Test
    public void testSelectionQuery() {
        PageMetadata.QueryResult q = page.metadata().selectionQuery();
        assertTrue( q.queryText().contains( "SELECT DISTINCT" ) );
        assertEquals( "http://environment.data.gov.uk/sparql/bwq/query", q.queryEndpoint() );
    }

    @Test
    public void testViewingQuery() {
        PageMetadata.QueryResult q = page.metadata().viewingQuery();
        assertTrue( q.queryText().contains( "CONSTRUCT" ) );
        assertEquals( "http://environment.data.gov.uk/sparql/bwq/query", q.queryEndpoint() );
    }

    @Test
    public void testProcessorName() {
        PageMetadata.Processor proc = page.metadata().processor();

        assertEquals( "Elda", proc.name() );
        assertEquals( "1.2.36-SNAPSHOT", proc.version() );
        assertEquals( "https://github.com/epimorphics/elda", proc.homePage() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

