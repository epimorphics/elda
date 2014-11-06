/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Encapsulates a view of an underlying Elda result set by specifying the properties
 * that should be presented for the resources selected into the result set.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class EldaView
extends CommonNodeWrapper
implements Comparable<EldaView>
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( EldaView.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new view specification resource with the given page and root.
     * @param page The page on which this view spec appears
     * @param root The root resource of the view specification
     */
    public EldaView( Page page, Resource root ) {
        super( page, root );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * @return The label of the view
     */
    public String label() {
        com.epimorphics.rdfutil.RDFNodeWrapper n = getPropertyValue( RDFS.label );
        return n == null ? null : n.getLexicalForm();
    }

    /**
     * @return The view's short name
     */
    public String viewName() {
        com.epimorphics.rdfutil.RDFNodeWrapper n = getPropertyValue( ELDA_API.viewName );
        return n == null ? null : n.getLexicalForm();
    }

    /**
     * Return a list of the property paths that are defined as being part of this view
     * @return A list of property path objects
     */
    public List<PropertyPath> propertyPaths() {
        List<PropertyPath> pp = new ArrayList<PropertyPath>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n: listPropertyValues( API.properties )) {
            pp.add( new PropertyPath( n.getLexicalForm() ) );
        }

        return pp;
    }

    /**
     * When comparing views in order to sort them, sort them in order of the view name
     * @param o An object to compare against
     * @return -1, 0, or 1 to indicate the relative order of the compared views
     */
    @Override
    public int compareTo( EldaView o ) {
        return viewName().compareTo( o.viewName() );
    }

    /**
     * @return The resource denoting the view that this EldaView is a version of
     */
    public com.epimorphics.rdfutil.RDFNodeWrapper isVersionOf() {
        return getPropertyValue( DCTerms.isVersionOf );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /**
     * Built-in view named <code>basic</code>.
     * Defined in <a href="http://code.google.com/p/linked-data-api/wiki/API_Viewing_Resources#Built-in_Viewers">the spec</a>.
     */
    public static class BasicView
    extends EldaView
    {
        public static final String NAME = "basic";

        private static final List<PropertyPath> basicPaths;
        private static final Resource fakeRoot = ResourceFactory.createResource();

        static {
            basicPaths = new ArrayList<PropertyPath>();
            basicPaths.add( new PropertyPath( "rdfs:label" ) );
            basicPaths.add( new PropertyPath( "rdf:type") );
        }

        public BasicView( Page page ) {
            super( page, fakeRoot );
        }

        @Override
        public String label() {
            return NAME;
        }

        @Override
        public List<PropertyPath> propertyPaths() {
            return basicPaths;
        }
    }

    /**
     * Built-in view named <code>all</code>.
     * Defined in <a href="http://code.google.com/p/linked-data-api/wiki/API_Viewing_Resources#Built-in_Viewers">the spec</a>.
     */
    public static class DescriptionView
    extends EldaView
    {
        public static final String NAME = "all";

        private static final List<PropertyPath> allPaths;
        private static final Resource fakeRoot = ResourceFactory.createResource();

        static {
            allPaths = new ArrayList<PropertyPath>();
            allPaths.add( new PropertyPath( "*" ) );
        }

        public DescriptionView( Page page ) {
            super( page, fakeRoot );
        }

        @Override
        public String label() {
            return NAME;
        }

        @Override
        public List<PropertyPath> propertyPaths() {
            return allPaths;
        }
    }
}

