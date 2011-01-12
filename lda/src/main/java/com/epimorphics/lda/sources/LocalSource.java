/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        LocalSource.java
    Created by:  Dave Reynolds
    Created on:  5 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIException;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 	Data source which represents an in-memory model loaded
 	from a local file. Used for testing. Model will be reloaded
 	from file each time this class is constructed!
 	
 	@author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class LocalSource implements Source {
    
    static Logger log = LoggerFactory.getLogger(LocalSource.class);

    public static final String PREFIX = "local:";
    
    protected Model source; 
    protected String endpoint;
    
    public LocalSource(String endpoint) {
        if (!endpoint.startsWith(PREFIX))
            throw new APIException("Illegal local endpoint: " + endpoint);
        source = FileManager.get().loadModel( endpoint.substring( PREFIX.length() ) );
        this.endpoint = endpoint;
    }
    
    @Override public QueryExecution execute(Query query) {
        if (log.isDebugEnabled()) {
            log.debug("Running query: " + query);
        }
        return QueryExecutionFactory.create(query, source);
    }
    
    public String toString() {
        return "Local datasource - " + endpoint;
    }
    
    /**
     	Add metdata describing this source to a metdata model 
    */
    public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, ResourceFactory.createResource(endpoint));
    }

}

