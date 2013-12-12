/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
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

import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.LockNone;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 * Data source representing and external SPARQL endpoint.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SparqlSource extends SourceBase implements Source {
    
    static Logger log = LoggerFactory.getLogger(SparqlSource.class);

    protected final String sparqlEndpoint;
    
    protected final Lock lock = new LockNone();
    
    protected Perhaps nestedSelects = Perhaps.DontKnow;
    
    protected enum Perhaps {Yes, No, DontKnow, CantTell}
    
    protected final String basicUser;
    
    protected final char[] basicPassword;
    
    public SparqlSource( Resource ep, AuthMap am ) {
    	super( ep );
    	String sparqlEndpoint = ep.getURI(); 
        this.sparqlEndpoint = sparqlEndpoint;
        String user = null;
        char [] password = null;
        if (ep != null) {
        	boolean b = RDFUtils.getBooleanValue( ep, EXTRAS.supportsNestedSelect, false );
        	nestedSelects = (b ? Perhaps.Yes : Perhaps.No);
        //
        	String authKey = RDFUtils.getStringValue( ep, EXTRAS.authKey, null );
        	// System.err.println( ">> AUTH KEY: " + authKey );
        	if (authKey != null) {
        		AuthInfo ai = am.get( authKey );
        		if (ai != null) {
        			user = ai.get("basic.user");
        			password = ai.get("basic.password").toCharArray();
        		}
        	}
        }
        this.basicUser = user;
        this.basicPassword = password;
        log.info( "created " + toString() );
    }
    
    @Override public QueryExecution execute(Query query) {
        if (log.isDebugEnabled()) log.debug("Running query on " + sparqlEndpoint + ":\n" + query);
		QueryEngineHTTP qe = new QueryEngineHTTP(sparqlEndpoint, query) ;
		// System.err.println( ">> basic user: " + basicUser );
		// System.err.println( ">> basic password: " + new String(basicPassword));
		if (basicUser != null) qe.setBasicAuthentication( basicUser, basicPassword );
		return qe ;
    }

    @Override public String toString() {
        return "SparqlSource{" + sparqlEndpoint + "; supportsNestedSelect: " + nestedSelects + "}";
    }
    
    @Override public Lock getLock() {
    	return lock;
    }
    
    /**
     * Add metadata describing this source to a metadata model 
     */
    @Override public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, ResourceFactory.createResource(sparqlEndpoint));
    }

    /**
        It's remote. Try if we can.
    */
	@Override public boolean supportsNestedSelect() {
		if (nestedSelects == Perhaps.DontKnow) probeNestedSelects();
		return nestedSelects == Perhaps.Yes;
	}

	/**
	    Should probe the remote end
	*/
	private void probeNestedSelects() {
		nestedSelects = Perhaps.CantTell;
	}
}

