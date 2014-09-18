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

import java.util.List;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.rdfutil.PropertyValue;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link DisplayRdfNode}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DisplayRdfNodeTest
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

    private DisplayRdfNode displayResource;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void before() {
        ResultsModel rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel ) );
        displayResource = new DisplayRdfNode( rm.page(), ResourceFactory.createResource( "http://www.treefroggames.com/a-few-acres-of-snow-2" ) );
    }


    @Test
    public void testGetDisplayProperties() {
        List<PropertyValue> displayTriples = displayResource.getDisplayProperties();
        String ns = "http://epimorphics.com/public/vocabulary/games.ttl#";

        assertEquals( 5, displayTriples.size() );
        assertEquals( "rdfs:label", displayTriples.get(0).getProp().getShortURI() );
        assertEquals( ns + "playTimeMinutes", displayTriples.get(1).getProp().getURI() );
        assertEquals( ns + "players", displayTriples.get(2).getProp().getURI() );
        assertEquals( ns + "pubYear", displayTriples.get(3).getProp().getURI() );
        assertEquals( ns + "designed-by", displayTriples.get(4).getProp().getURI() );
    }

    @Test
    public void testTypes() {
        List<RDFNodeWrapper> types = displayResource.types();
        String ns = "http://epimorphics.com/public/vocabulary/games.ttl#";

        assertEquals( 1, types.size() );
        assertEquals( ns + "BoardGame", types.get(0).getURI() );
    }

    @Test
    public void testLabels() {
        List<RDFNodeWrapper> labels = displayResource.labels();

        assertEquals( 1, labels.size() );
        assertEquals( "A Few Acres of Snow", labels.get(0).getLexicalForm() );

    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

