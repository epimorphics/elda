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

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.rdfutil.DatasetWrapper;
import com.epimorphics.rdfutil.ModelWrapper;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link CommonNodeWrapper}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class CommonNodeWrapperTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    static final Model testModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES +
            "@prefix example: <http://example/foo#>. " +
            "example:foo example:p \"42\"^^xsd:int; " +
            "            example:q \"42\"^^xsd:string;" +
            "            example:r example:bar."
            );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Test
    public void testGetInt() {
        String ns = "http://example/foo#";
        ModelWrapper mw = modelWrapperFixture();
        Resource foo = mw.getModel().getResource( ns + "foo" );
        CommonNodeWrapper n = new CommonNodeWrapper( mw, foo );

        // various ways of specifying p
        assertEquals( 42, n.getInt( mw.getModel().getProperty( ns+"p" ), -1 ) );
        assertEquals( 42, n.getInt( ns+"p", -1 ) );
        assertEquals( 42, n.getInt( "example:p", -1 ) );

        // missing property
        assertEquals( -123, n.getInt( "example:not-here", -123 ) );

        // node is not a resource
        CommonNodeWrapper l = new CommonNodeWrapper( mw, mw.getModel().createLiteral( "kermit" ) );
        assertEquals( -1, l.getInt( "example:foo", -1 ));

        // value is not an int
        assertEquals( -111, n.getInt( "example:q", -111 ));
    }

    @Test
    public void testGetResource() {
        String ns = "http://example/foo#";
        ModelWrapper mw = modelWrapperFixture();
        Resource foo = mw.getModel().getResource( ns + "foo" );
        CommonNodeWrapper n = new CommonNodeWrapper( mw, foo );

        // various ways of specifying p
        assertEquals( ResourceFactory.createResource( ns + "bar" ), n.getResource( mw.getModel().getProperty( ns+"r" ) ) );
        assertEquals( ResourceFactory.createResource( ns + "bar" ), n.getResource( ns+"r" ) );
        assertEquals( ResourceFactory.createResource( ns + "bar" ), n.getResource( "example:r" ) );

        // missing property
        assertNull( n.getResource( "example:not-here" ) );

        // node is not a resource
        CommonNodeWrapper l = new CommonNodeWrapper( mw, mw.getModel().createLiteral( "kermit" ) );
        assertNull( l.getResource( "example:p" ) );

        // value is not a resource
        assertNull( n.getResource( "example:p" ));
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    private ModelWrapper modelWrapperFixture() {
        Dataset ds = DatasetFactory.create( testModel );
        DatasetWrapper dsw = new DatasetWrapper( ds );
        return new ModelWrapper( dsw );
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

