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

import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.Property;
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
    public List<Statement> orderProperties( RDFNodeWrapper subject ) {
        List<Statement> triples = subject.asResource().listProperties().toList();
        final ModelWrapper model = subject.getModelW();
        final Map<Property, String> propertyNames = propertyNames( triples, model );

        Collections.sort( triples, new Comparator<Statement>() {
            @Override
            public int compare( Statement o1, Statement o2 ) {
                String p1Label = propertyNames.get( o1.getPredicate() );
                String p2Label = propertyNames.get( o2.getPredicate() );

                return p1Label.compareTo( p2Label );
            }
        } );

        return triples;
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Return a map from the predicates in the given set of triples to their string labels
     *
     * @param triples A list of RDF triples
     * @param model Model wrapper
     * @return A map from each of the distinct predicates in <code>triples</code> to its
     * corresponding label
     */
    private Map<Property, String> propertyNames( List<Statement> triples, ModelWrapper model ) {
        Map<Property, String> names = new HashMap<Property, String>();

        for (Statement s: triples) {
            Property p = s.getPredicate();
            if (!names.containsKey( p )) {
                String pLabel = new RDFNodeWrapper( model, p ).getName();
                names.put( p, pLabel );
            }
        }

        return names;
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

