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

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Unit tests for {@link ResultsModel}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class ResultsModelTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( ResultsModelTest.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        // we are forced to use the legacy imposteriser because APIResultSet does not
        // have an interface that it conforms to
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Test
    public void testCreateResultsModel() {
        ResultsModel rm = new ResultsModel( mockResultSet() );
        assertFalse( rm.getModel().isEmpty() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    static final Model apiResultsModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_BWQ );

    /**
     * Create an APIResultSet fixture without trying to do all that that very complex
     * class does.
     * @return Mocked {@link APIResultSet}
     */
    protected APIResultSet mockResultSet() {
        final APIResultSet results = context.mock( APIResultSet.class );
        final APIResultSet.MergedModels mm = context.mock( APIResultSet.MergedModels.class );

        context.checking(new Expectations() {{
            atLeast(1).of (results).getModels();
            will( returnValue( mm ) );

            atLeast(1).of (mm).getMergedModel();
            will( returnValue( apiResultsModel ));
        }});

        return results;
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

