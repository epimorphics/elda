/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.vocabularies.*;
import com.epimorphics.lda.vocabularies.ELDA.COMMON;
import com.epimorphics.lda.vocabularies.ELDA.DOAP_EXTRAS;
import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Facade class for providing easier access to the elements of the
 * metadata attached to a given {@link Page} of results.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class PageMetadata
extends ModelWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    private static final Logger log = LoggerFactory.getLogger( PageMetadata.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The page that this is the metadata for */
    Page page;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a PageMetadata facade for the given {@link Page}, by extracting
     * the metadata model.
     *
     * @param page
     */
    public PageMetadata( Page page ) {
        super( page.getModelW().getDataset().getNamedModel( ResultsModel.RESULTS_METADATA_GRAPH ) );
        this.page = page;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /** @return The RDF resource which is the root of statements about this page's metadata */
    public RDFNodeWrapper pageRoot() {
        ResIterator i = getModel().listSubjectsWithProperty( RDF.type, API.Page );

        if (!i.hasNext()) {
            throw new EldaException( "Unexpected: page metadata has no resource with rdf:type api:Page" );
        }

        RDFNodeWrapper pageRoot = new RDFNodeWrapper( this, i.next() );

        if (i.hasNext()) {
            log.warn(ELog.message("unexpected: page metadata has more than one rdf:type api:Page resource - %s", i.next()));
        }

        return pageRoot;
    }

    /** @return The execution object which documents the generation of this page of results */
    public Execution execution() {
        return new Execution( this, pageRoot().getPropertyValue( API.wasResultOf ).asResource() );
    }

    /** @return The query result object documenting the selection query */
    public QueryResult selectionQuery() {
        return execution().selectionQuery();
    }

    /** @return The query result object documenting the viewing query */
    public QueryResult viewingQuery() {
        return execution().viewingQuery();
    }

    /** @return The processor which generated the resultset */
    public Processor processor() {
        return execution().processor();
    }

    /** @return The SPARQL endpoint to use for queries against this page, preferring the declared visible endpoint */
    public String sparqlEndpoint( VelocityContext context ) {
        if (context.containsKey( "visibleSparqlEndpoint" )) {
            return ((Value) context.get( "visibleSparqlEndpoint" )).spelling();
        }
        else if (selectionQuery() != null) {
            return selectionQuery().queryEndpoint();
        }
        else if (viewingQuery() != null) {
            return viewingQuery().queryEndpoint();
        }
        else {
            return null;
        }
    }
    
    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /** Encapsulates the execution object which produced the page's results */
    public static class Execution
    extends CommonNodeWrapper
    {
        public Execution( ModelWrapper meta, Resource executionRoot ) {
            super( meta, executionRoot );
        }

        public QueryResult selectionQuery() {
            return queryResult( API.selectionResult );
        }

        public QueryResult viewingQuery() {
            return queryResult( API.viewingResult );
        }

        public Processor processor() {
            return new Processor( getModelW(), getPropertyValue( API.processor ).asResource() );
        }

        protected QueryResult queryResult( Property queryProperty ) {
            RDFNodeWrapper r = getPropertyValue( queryProperty );

            return (r == null) ? null : new QueryResult( getModelW(), r.asResource() );
        }
    }

    /** Encapsulates the query result object which documents a SPARQL query run to create the page */
    public static class QueryResult
    extends CommonNodeWrapper
    {
        public QueryResult( ModelWrapper meta, Resource queryResultRoot ) {
            super( meta, queryResultRoot );
        }

        public String queryText() {
            return getPropertyValue( SPARQL.query ).getPropertyValue( RDF.value ).getLexicalForm();
        }

        public String queryEndpoint() {
            return getPropertyValue( SPARQL.endpoint ).getPropertyValue( API.sparqlEndpoint ).getLexicalForm();
        }
    }

    /** Encapsulates the processor that generated the result set */
    public static class Processor
    extends CommonNodeWrapper
    {
        public Processor( ModelWrapper meta, Resource processorRoot ) {
            super( meta, processorRoot );
        }

        public String name() {
            return lookupString( new Property[]{COMMON.software, DOAP_EXTRAS.releaseOf, RDFS.label}, "processor name" );
        }

        public String homePage() {
            return lookupString( new Property[]{COMMON.software, DOAP_EXTRAS.releaseOf, DOAP.homepage}, "processor home page" );
        }

        public String version() {
            return lookupString( new Property[]{COMMON.software, DOAP.revision}, "processor version" );
        }

        protected String lookupString( Property[] path, String lookingFor ) {
            RDFNodeWrapper n = this;

            for (Property p: path) {
                if (n != null) {
                    n = n.getPropertyValue( p );
                }
            }

            return (n == null) ? (lookingFor + " not found") : n.getLexicalForm();
        }
    }
}

