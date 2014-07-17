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

import com.epimorphics.lda.vocabularies.*;
import com.epimorphics.rdfutil.ModelWrapper;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Value object representing the page of results returned by Elda's query
 * processing. Corresponds to a single resource of type <code>api:Page</code>.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class Page extends CommonNodeWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** Indicate no available numeric value */
    public static final int NO_VALUE = -1;

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( Page.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new page object corresponding to the <code>root</code>
     * object in model <code>mw</code>.
     * @param mw A wrapper around the model containing the results from the API
     * @param root The root resource of this page
     */
    public Page( ModelWrapper mw, Resource root ) {
        super( mw, root );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    public Page getPage() {
        return this;
    }

    /**
     * @return True if the page denotes a single item, or false for a list endpoint
     */
    public boolean isItemEndpoint() {
        return asResource().hasProperty( FOAF.primaryTopic );
    }

    /**
     * @return The current page number, starting from zero. Return -1 if this page
     * does not have a specified page number
     */
    public int pageNumber() {
        return getInt( API.page, NO_VALUE );
    }

    /**
     * @return The number of items per page. Return -1 if this page does not
     * specifiy the number of items per page
     */
    public int itemsPerPage() {
        return getInt( OpenSearch.itemsPerPage, NO_VALUE );
    }

    /**
     * @return The starting index for this page, starting from one. Return -1
     * if this page does not specify the starting index.
     */
    public int startIndex() {
        return getInt( OpenSearch.startIndex, NO_VALUE );
    }

    /**
     * @return The list that this page is part of
     */
    public Resource isPartOf() {
        return getResource( DCTerms.isPartOf );
    }

    /**
     * @return A resource denoting the API endpoint specification.
     */
    public Resource definition() {
        return getResource( API.definition );
    }

    /**
     * @return The URL for the extended metadata for this page
     */
    public String extendedMetadataURL() {
        com.epimorphics.rdfutil.RDFNodeWrapper nw = getPropertyValue( API.extendedMetadataVersion );
        return (nw == null) ? null : nw.getLexicalForm();
    }

    /**
     * @return The resource denoting the first page of results, or null
     */
    public Resource firstPage() {
        return getResource( XHV.first );
    }

    /**
     * @return The resource denoting the previous page of results, or null
     */
    public Resource prevPage() {
        return getResource( XHV.prev );
    }

    /**
     * @return The resource denoting the next page of results, or null
     */
    public Resource nextPage() {
        return getResource( XHV.next );
    }

    /**
     * @return The resource denoting the last page of results, or null
     */
    public Resource lastPage() {
        return getResource( ResourceFactory.createProperty( XHV.ns + "last" ) );
    }

    /**
     * @return A list of the formats that this page is available in
     */
    public List<PageFormat> formats() {
        List<PageFormat> pfs = new ArrayList<PageFormat>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n: listPropertyValues( DCTerms.hasFormat )) {
            pfs.add( new PageFormat( this, n.asResource() ) );
        }

        Collections.sort( pfs );

        return pfs;
    }

    /**
     * @return A list of the views that this page defines
     */
    public List<EldaView> views() {
        List<EldaView> vs = new ArrayList<EldaView>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n: listPropertyValues( DCTerms.hasVersion )) {
            vs.add( new EldaView( this, n.asResource() ) );
        }

        Collections.sort( vs );

        return vs;
    }

    /**
     * Return a list of the term bindings, which associate a short name with a resource.
     * @return A list of the term bindings defined on this page
     */
    public List<Binding<Resource>> termBindings() {
        List<Binding<Resource>> bindings = new ArrayList<Binding<Resource>>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n: connectedNodes( "api:wasResultOf/api:termBinding" )) {
            String label = n.getPropertyValue( API.label ).getLexicalForm();
            Resource res = n.getPropertyValue( API.property ).asResource();

            bindings.add( new Binding<Resource>( label, res ) );
        }

        return bindings;
    }

    /**
     * Return a list of the variable bindings, which associate a variable name with a textual value
     * @return A list of the variable bindings
     */
    public List<Binding<String>> varBindings() {
        List<Binding<String>> bindings = new ArrayList<Binding<String>>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n: connectedNodes( "api:wasResultOf/api:variableBinding" )) {
            String label = n.getPropertyValue( API.label ).getLexicalForm();
            String value = n.getPropertyValue( API.value).getLexicalForm();

            bindings.add( new Binding<String>( label, value ) );
        }

        return bindings;
    }

    /**
     * @return The {@link #varBindings()} as a map
     */
    public Map<String,String> varBindingsMap() {
        Map<String,String> map = new HashMap<String, String>();

        for (Binding<String> binding: varBindings()) {
            map.put( binding.label(), binding.value() );
        }

        return map;
    }

    /**
     * @return The resource that describes the version of Elda used to generate this page
     */
    public com.epimorphics.rdfutil.RDFNodeWrapper eldaProcessor() {
        return connectedNodes( "api:wasResultOf/api:processor/<http://purl.org/net/opmv/types/common#software>" ).get( 0 );
    }

    /**
     * @return The readable label for the version of Elda used to produce this page
     */
    public String eldaLabel() {
        return eldaProcessor().getPropertyValue( RDFS.label ).getLexicalForm();
    }

    /**
     * @return The version string for the release of Elda used to produce this page
     */
    public String eldaVersion() {
        return eldaProcessor().getPropertyValue( DOAP.revision ).getLexicalForm();
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

