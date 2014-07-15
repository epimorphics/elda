/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Encapsulates a format that a page is declared as being available in.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PageFormat extends RDFNodeWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( PageFormat.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The page object for which this format is one of the available formats */
    private Page page;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a page format whose configuration root is the given resource.
     *
     * @param page The page object this is one of the formats for
     * @param root The configuration root resource
     */
    public PageFormat( Page page, Resource root ) {
        super( page.getModelW(), root );
        this.page = page;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

