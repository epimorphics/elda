/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


/**
 * Denotes a binding of a string to some value type
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class Binding<BoundType>
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

    private String label;
    private BoundType value;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public Binding( String label, BoundType value ) {
        this.label = label;
        this.value = value;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    public String label() {
        return this.label;
    }

    public BoundType value() {
        return this.value;
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

