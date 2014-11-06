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

import com.epimorphics.rdfutil.*;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * The default property ordering strategy is to order the triples according
 * to a lexical sort by property label.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DefaultPropertyOrderingStrategy
implements PropertyOrderingStrategy
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( DefaultPropertyOrderingStrategy.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Return the triples whose subject resource is <code>subject</code>, sorted by
     * the label on the triple's predicate.
     * @param subject A subject resource
     * @return The triples whose subject is <code>subject</code>, sorted by label order.
     */
    @Override
    public List<PropertyValue> orderProperties( RDFNodeWrapper subject ) {
        List<PropertyValue> apvs = collectValues( subject );
        final Map<RDFNodeWrapper, String> propertyNames = propertyNames( apvs, subject.getModelW() );

        orderSelectedProperties( apvs, propertyNames );

        orderPropertyMultiValues( apvs );

        return apvs;
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Given a collection of selected properties, put them into the required order. In this default
     * ordering we sort by name.
     *
     * @param propertyValues
     * @param propertyNames
     */
    protected void orderSelectedProperties( List<PropertyValue> propertyValues, final Map<RDFNodeWrapper, String> propertyNames ) {
        Collections.sort( propertyValues, new Comparator<PropertyValue>() {
            @Override
            public int compare( PropertyValue o1, PropertyValue o2 ) {
                String p1Label = propertyNames.get( o1.getProp() );
                String p2Label = propertyNames.get( o2.getProp() );

                return p1Label.compareToIgnoreCase( p2Label );
            }
        } );
    }

    /**
     * Ensure that the values of multi-value properties are placed in a suitable order.
     *
     * @param pvs A list of property-value pairs to be placed into order
     */
    protected void orderPropertyMultiValues( List<PropertyValue> pvs ) {
        // PropertyValue contains a list of values, we need these to be in order as well
        for (PropertyValue pv: pvs) {
            Collections.sort( pv.getValues(), new Comparator<RDFNodeWrapper>() {
                @Override
                public int compare( RDFNodeWrapper o1, RDFNodeWrapper o2 ) {
                    return o1.getName().compareTo( o2.getName() );
                }
            } );
        }
    }

    /**
     * Collect the property value pairs of the given subject, and return a list
     * @param subject An RDF resource node
     * @return A list of the property-value pairs whose subject is <code>subject</code>
     */
    protected List<PropertyValue> collectValues( RDFNodeWrapper subject ) {
        PropertyValueSet pvs = new PropertyValueSet( subject.getModelW() );

        for (Statement triple: subject.asResource().listProperties().toList()) {
            pvs.add( triple );
        }

        return pvs.getValues();
    }

    /**
     * Return a map from the predicates in the given set of triples to their string labels
     *
     * @param triples A list of RDF triples
     * @param model Model wrapper
     * @return A map from each of the distinct predicates in <code>triples</code> to its
     * corresponding label
     */
    private Map<RDFNodeWrapper, String> propertyNames( List<PropertyValue> apvs, ModelWrapper model ) {
        Map<RDFNodeWrapper, String> names = new HashMap<RDFNodeWrapper, String>();

        for (PropertyValue pv: apvs) {
            RDFNodeWrapper p = pv.getProp();
            if (!names.containsKey( p )) {
                names.put( p, p.getName() );
            }
        }

        return names;
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

