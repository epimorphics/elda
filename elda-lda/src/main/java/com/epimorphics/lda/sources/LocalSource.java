/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.Locator;

/**
 	Data source which represents an in-memory model loaded
 	from a local file. Used for testing. Model will be reloaded
 	from file each time this class is constructed!
 	
 	@author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class LocalSource extends SourceBase implements Source {
    
    static Logger log = LoggerFactory.getLogger(LocalSource.class);

    public static final String PREFIX = "local:";
    
    protected final Model source; 
    protected final String endpoint;
    
    
    public LocalSource( FileManager fm, String endpoint ) {
        if (!endpoint.startsWith(PREFIX))
            throw new APIException("Illegal local endpoint: " + endpoint);
        this.source = fm.loadModel( endpoint.substring( PREFIX.length() ) );
        this.endpoint = endpoint;
    }
    
    @Override public QueryExecution execute(Query query) {
        if (log.isDebugEnabled()) log.debug("Running query: " + query);
        return QueryExecutionFactory.create(query, source);
    }
    
    @Override public Lock getLock() {
    	return source.getLock();
    }
    
    @Override public String toString() {
        return "Local datasource - " + endpoint;
    }
    
    /**
     	Add metdata describing this source to a metdata model 
    */
    @Override public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, ResourceFactory.createResource(endpoint));
    }

    /**
        Local sources are held in in-memory models and support nested selects
        via ARQ.
    */
	@Override public boolean supportsNestedSelect() {
		return true;
	}

}

