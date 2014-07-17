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

import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.rdfutil.RDFUtil;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Facade for a resource from the resultset with methods to make displaying
 * the resource in a template easier. In particular, this class collaborates
 * with {@link DisplayHierarchy} to unfold the results graph into a tree
 * that can be displayed to the user.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DisplayResource
extends CommonNodeWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( DisplayResource.class );

    /** The strategy object which we will use to order the resource's triples for display */
    private static PropertyOrderingStrategy propertyOrdering;

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a displayable resource attached to the given page.
     * @param page The page object
     * @param root The RDF resource root
     */
    public DisplayResource( Page page, Resource root ) {
        super( page, root );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Set the new common strategy for ordering the triples attached to a property.
     * @param ordering The new {@link PropertyOrderingStrategy}
     */
    public static void setPropertyOrdering( PropertyOrderingStrategy ordering ) {
        propertyOrdering = ordering;
    }

    /**
     * @return The current common property ordering strategy
     */
    public static PropertyOrderingStrategy propertyOrdering() {
        if (propertyOrdering == null) {
            propertyOrdering = new DefaultPropertyOrderingStrategy();
        }

        return propertyOrdering;
    }

    /**
     * Return a list of the triples for this resource which should be displayed,
     * in the order in which they should be displayed.
     * @return A list of RDF triples in display order
     */
    public List<Statement> getDisplayProperties() {
        List<Statement> triples = propertyOrdering().orderProperties( this );
        return withoutNonDisplayTriples( triples );
    }

    /**
     * Return the list of types - i.e. values of <code>rdf:type</code> of this
     * resource, sorted into label order.
     * @return An ordered list of this resources types
     */
    public List<RDFNodeWrapper> types() {
        List<RDFNodeWrapper> ts = listPropertyValues( RDF.type );

        Collections.sort( ts, new Comparator<RDFNodeWrapper>() {
            @Override
            public int compare( RDFNodeWrapper o1, RDFNodeWrapper o2 ) {
                return o1.getName().compareTo( o2.getName() );
            }
        });

        return ts;
    }

    /**
     * Return a list of all of the labels for this resource. This will include
     * all of the labelling properties from {@link RDFUtil#labelProps}
     * @return A list of all of the resource's labels
     */
    public List<RDFNodeWrapper> labels() {
        List<RDFNodeWrapper> labels = new ArrayList<RDFNodeWrapper>();

        for (Property labelProp: RDFUtil.labelProps) {
            labels.addAll( listPropertyValues( labelProp ) );
        }

        Collections.sort( labels, new Comparator<RDFNodeWrapper>() {
            @Override
            public int compare( RDFNodeWrapper o1, RDFNodeWrapper o2 ) {
                return o1.getLexicalForm().compareTo( o2.getLexicalForm() );
            }
        });

        return labels;
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Side-effect the given list of triples to remove those that are not to
     * be displayed.
     *
     * @param triples A list of RDF triples from which the non-display triples
     * will be removed.
     */
    protected List<Statement> withoutNonDisplayTriples( List<Statement> triples ) {
        List<Statement> dTriples = new ArrayList<>( triples.size() );

        for (Statement s: triples) {
            if (!isNonDisplay( s )) {
                dTriples.add( s );
            }
        }

        return dTriples;
    }

    /**
     * Return true if the given triple should not appear in the list of display
     * triples for a resource. Currently, this equates to the predicate being
     * rdf:type.
     * @param s A triple
     * @return True if the triple is not part of the resources displayed triples
     */
    protected boolean isNonDisplay( Statement s ) {
        return s.getPredicate().equals( RDF.type );
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

