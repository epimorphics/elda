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

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.rdfutil.*;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * An facade for the {@link APIResultSet} returned from Elda processing,
 * which is also a {@link ModelWrapper} that collaborates with the RDF
 * utility methods from the Epimorphics library.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class ResultsModel extends ModelWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** Graph name for the graph of actual results data */
    public static final String RESULTS_OBJECT_GRAPH = API.NS + "results_object_graph";

    /** Graph name for the graph of results metadata */
    public static final String RESULTS_METADATA_GRAPH = API.NS + "results_metadata_graph";

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( ResultsModel.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The API results, as given */
    private APIResultSet results;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public ResultsModel( APIResultSet results ) {
        super( asDataset( results ) );
        this.results = results;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Return a {@link Page} object decorating the underlying page resource in
     * the API results model
     * @return A page object
     */
    public Page page() {
        return new Page( this, results.getRoot().inModel( this.getModel() ) );
    }

    /**
     * @return The metadata model for this results set
     */
    public Model metadataModel() {
        return results.getModels().getMetaModel();
    }
    
    /**
     * @return The page root resource, but attached only to the metadata model
     */
    public RDFNodeWrapper metadataRoot() {
        return new RDFNodeWrapper( new ModelWrapper( metadataModel() ), results.getRoot().inModel( metadataModel() ) );
    }
    
    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Return a {@link DatasetWrapper} for the given resultset, in which
     * the two embedded graphs are made named-graph members.
     * @return A dataset wrapper presenting the API results combined model as a dataset
     */
    protected static DatasetWrapper asDataset( APIResultSet results ) {
        APIResultSet.MergedModels mm = results.getModels();
        Dataset ds = DatasetFactory.create( mm.getMergedModel() );

        ds.addNamedModel( RESULTS_OBJECT_GRAPH, mm.getObjectModel() );
        ds.addNamedModel( RESULTS_METADATA_GRAPH, mm.getMetaModel() );

        return new DatasetWrapper( ds, false, mm.getMergedModel() );
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

