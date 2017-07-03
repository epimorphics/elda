/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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

import com.epimorphics.lda.textsearch.TextSearchConfig;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * Abstraction of the data endpoint to be queried.
 * Can be a remote SPARQL service, a restful endpoint or some local model
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface Source {

	/**
        Return a name for this source, used for error reporting
    */
    @Override public String toString();
    
    /**
        Add metdata describing this source to a metdata model 
    */
    public void addMetadata( Resource meta );
        
    /**
        Do a DESCRIBE. Answer the resulting model. Puts a read
        lock around the access to the underlying data.
    */
    public Model executeDescribe( Query q );
        
    /**
        Do a CONSTRUCT. Answer the resulting model. Puts a read
        lock around the access to the underlying data.
    */
    public Model executeConstruct( Query q );
        
    /**
        Do a SELECT. First run c.setup on the QueryExecution
        object created from the query. Then run c.consume on the
        ResultSet of the select. Puts a read lock around the access 
        to the underlying data.
    */
    public void executeSelect( Query q, ResultSetConsumer c );
    
    /**
        The callback object used for SELECTs.
    */
    public interface ResultSetConsumer {
    	void setup( QueryExecution qe );
    	void consume( ResultSet rs );
    	}
	
	/**
	    Answer this Source's text search configuration.
	*/
	public TextSearchConfig getTextSearchConfig();
	
}

