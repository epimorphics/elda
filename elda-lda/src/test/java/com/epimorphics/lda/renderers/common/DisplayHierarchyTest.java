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
import org.junit.Rule;
import org.junit.Test;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Unit tests for {@link DisplayHierarchy}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DisplayHierarchyTest
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


    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Test
    public void testExpand() {
        DisplayHierarchy dh = fixture1();
        dh.expand();
        assertEquals( 10, dh.roots().size() );
    }

    @Test
    public void testContextSeen() {
        DisplayHierarchy.DisplayHierarchyContext ctx = new DisplayHierarchy.DisplayHierarchyContext();
        Model m = ModelFactory.createDefaultModel();
        ModelWrapper mw = new ModelWrapper( m );
        RDFNodeWrapper rn = new RDFNodeWrapper( mw, m.createResource( "http://example.com/foo" ));
        
        assertFalse( ctx.isSeen( rn ) );
        ctx.see( rn );
        assertTrue( ctx.isSeen( rn ) );
    }
    
    @Test
    public void testExplicitPaths() {
        // we need to set an explicit _properties variable in the context model
        DisplayHierarchy dh = fixture2();
        dh.expand();
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    private DisplayHierarchy fixture1() {
        return fixture(  Fixtures.PAGE_BWQ_MODEL );
    }

    private DisplayHierarchy fixture2() {
        return fixture(  Fixtures.PAGE_BWQ_PROPERTIES_MODEL );
    }

    private DisplayHierarchy fixture( Model fixtureModel ) {
        Model apiMetadataModel = ModelFactory.createDefaultModel();

        APIResultSet resultSet = Fixtures.mockResultSet( context, fixtureModel, fixtureModel, apiMetadataModel );
        ResultsModel rm = new ResultsModel( resultSet );
        Page page = rm.page();
        page.initialiseShortNameRenderer( Fixtures.shortNameServiceFixture() );

        return new DisplayHierarchy( page );
    }

    
    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

