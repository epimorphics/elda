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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.rdfutil.*;
import org.apache.jena.rdf.model.*;

/**
 * TODO class comment
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DefaultPropertyOrderingStrategyTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( DefaultPropertyOrderingStrategyTest.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    private Model m = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES +
                                                    "@prefix test: <http://example/test#>.\n"
                                                    + "test:subj test:p1 test:foo ; test:p2 test:bar ; test:p3 test:fubar.\n"
                                                    + "test:p1 rdfs:label 'yy last'.\n"
                                                    + "test:p3 skos:prefLabel 'xx second'." );

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Test
    public void testOrderProperties() {
        Resource subj = m.getResource( "http://example/test#subj" );
        ModelWrapper mw = new ModelWrapper( m );
        RDFNodeWrapper subjw = new RDFNodeWrapper( mw, subj );

        List<PropertyValue> triples = new DefaultPropertyOrderingStrategy().orderProperties( subjw );

        assertEquals( "http://example/test#p2", triples.get(0).getProp().getURI() );
        assertEquals( "http://example/test#p3", triples.get(1).getProp().getURI() );
        assertEquals( "http://example/test#p1", triples.get(2).getProp().getURI() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

