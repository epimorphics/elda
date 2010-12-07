/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        SparqlSource.java
    Created by:  Dave Reynolds
    Created on:  2 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Data source representing and external SPARQL endpoint.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SparqlSource implements Source {
    
    static Logger log = LoggerFactory.getLogger(SparqlSource.class);

    protected String sparqlEndpoint;
    
    public SparqlSource(String sparqlEndpoint) {
        this.sparqlEndpoint = sparqlEndpoint;
        log.info( "created SparqlSource{" + sparqlEndpoint + "}" );
    }
    
    @Override public QueryExecution execute(Query query) {
        if (log.isInfoEnabled()) {
            log.info("Running query on " + sparqlEndpoint + ":\n" + query);
        }
        return QueryExecutionFactory.sparqlService(sparqlEndpoint, query);
    }

    public String toString() {
        return "SparqlSource{" + sparqlEndpoint + "}";
    }
    
    /**
     * Add metdata describing this source to a metdata model 
     */
    public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, ResourceFactory.createResource(sparqlEndpoint));
    }
}

