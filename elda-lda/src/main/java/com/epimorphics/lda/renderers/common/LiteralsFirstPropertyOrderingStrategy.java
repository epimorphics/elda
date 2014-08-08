/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.rdfutil.PropertyValue;
import com.epimorphics.rdfutil.RDFNodeWrapper;

/**
 * A {@link PropertyOrderingStrategy} that is similar to {@link DefaultPropertyOrderingStrategy},
 * except that all literal-valued properties are placed before resource-valued properties
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class LiteralsFirstPropertyOrderingStrategy
extends DefaultPropertyOrderingStrategy
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( LiteralsFirstPropertyOrderingStrategy.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Given a collection of selected properties, put them into the required order. In this
     * ordering we put literal-valued properties first before resource-valued properties; within
     * these two groups values are sorted by property label.
     *
     * @param propertyValues
     * @param propertyNames
     */
    @Override
    protected void orderSelectedProperties( List<PropertyValue> propertyValues, final Map<RDFNodeWrapper, String> propertyNames ) {
        Collections.sort( propertyValues, new Comparator<PropertyValue>() {
            @Override
            public int compare( PropertyValue o1, PropertyValue o2 ) {
                boolean lv1 = literalValued( o1 );
                boolean lv2 = literalValued( o2 );

                if (lv1 && !lv2) {
                    return -1;
                }
                else if (lv2 && !lv1) {
                    return 1;
                }
                else {
                    String p1Label = propertyNames.get( o1.getProp() );
                    String p2Label = propertyNames.get( o2.getProp() );

                    return p1Label.compareToIgnoreCase( p2Label );
                }
            }

            /** @return true if all values of the pv are literals */
            protected boolean literalValued( PropertyValue pv ) {
                boolean literals = true;
                for (RDFNodeWrapper node: pv.getValues()) {
                    literals = literals && node.isLiteral();
                }
                return literals;
            }
        } );
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

