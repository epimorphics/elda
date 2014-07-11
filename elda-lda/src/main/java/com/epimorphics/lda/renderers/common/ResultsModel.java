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
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.rdfutil.DatasetWrapper;
import com.epimorphics.rdfutil.ModelWrapper;
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
    public static final String RESULTS_OBJECT_GRAPH = EXTRAS.NS + "results_object_graph";

    /** Graph name for the graph of results metadata */
    public static final String RESULTS_METADATA_GRAPH = EXTRAS.NS + "results_metadata_graph";

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( ResultsModel.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public ResultsModel( APIResultSet results ) {
        super( asDataset( results ) );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Return a {@link DatasetWrapper} for the given resultset, in which
     * the two embedded graphs are made named-graph members.
     * @return A dataset wrapper presenting the API results combined model as a dataset
     */
    protected static DatasetWrapper asDataset( APIResultSet results ) {
        Model mm = results.getModels().getMergedModel();
        Dataset ds = DatasetFactory.create( mm );

        // TODO ideally, we would break out the object and results graphs from
        // APIResultSet as separate graphs in the dataset at this point. However,
        // APIResultSet.MergedModel's contract is unclear. Need to discuss this
        // with Chris

        return new DatasetWrapper( ds, false, mm );
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

