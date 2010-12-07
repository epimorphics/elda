/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        Source.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.sources;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Abstraction of the data endpoint to be queried.
 * Can be a remote SPARQL service, a restful endpoint or some local model
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface Source {

    /**
     * Set up and execution of the given query against the source.
     */
    public QueryExecution execute(Query query);
    
    /**
     * Return a name for this source, used for error reporting
     */
    public String toString();
    
    /**
     * Add metdata describing this source to a metdata model 
     */
    public void addMetadata(Resource meta);
}

