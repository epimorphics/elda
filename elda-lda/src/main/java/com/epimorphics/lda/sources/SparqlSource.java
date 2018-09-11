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

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.RDFUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockNone;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

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
    
    protected final String basicUser;
    
    protected final char[] basicPassword;
    
    public SparqlSource( Resource ep, AuthMap am ) {
    	super( ep );
    	String sparqlEndpoint = ep.getURI(); 
    	boolean secure = sparqlEndpoint.startsWith("https:");
        this.sparqlEndpoint = sparqlEndpoint;
        String user = null;
        char [] password = null;
        boolean allowInsecure = false;
    //
        if (ep != null) {
        	allowInsecure = RDFUtils.getBooleanValue(ep, ELDA_API.authAllowInsecure, false);
        //
        	String authKey = RDFUtils.getStringValue( ep, ELDA_API.authKey, null );
        	if (authKey != null) {
//        		System.err.println(">> authKey: " + authKey);
//        		System.err.println(">> authMap: " + am);
				log.debug("handling auth key '{}'", authKey);
        		AuthInfo ai = am.get( authKey );
        		        		
        		if (ai != null) {
        			user = ai.get("basic.user");
        			password = ai.get("basic.password").toCharArray();
        		}
        	}
        }
    //
        this.basicUser = user;
        this.basicPassword = password;

//        System.err.println( ">> basicUser:     " + this.basicUser);
//        System.err.println( ">> secure:        " + secure);
//        System.err.println( ">> allowInsecure: " + allowInsecure);
        
        if (this.basicUser != null && !secure && !allowInsecure) {
        	throw new EldaException
        		( "This basic-authentication SPARQL endpoint (" 
        		+ sparqlEndpoint + ")\n is insecure (does not use https:)"
        		+ "\n and authAllowInsecure has not been specified."
        		);
        }
    //
        log.info("created '{}'", this.toString());
    }
    
    @Override public QueryExecution execute(Query query) {
        if (log.isDebugEnabled()) log.debug("running query on '{}':\n{}", sparqlEndpoint, query);
		QueryEngineHTTP qe = new QueryEngineHTTP(sparqlEndpoint, query);
		if (basicUser != null) {
			log.debug("basic user '{}'", basicUser);			
			log.debug("basic password '{}'", new String(basicPassword));
			qe.setBasicAuthentication( basicUser, basicPassword );
		}
		return qe ;
    }

    @Override public String toString() {
        return 
        	"SparqlSource{" + sparqlEndpoint 
        	+ (this.basicUser == null ? "; unauthenticated" : "; basic authentication")
        	+ "}";
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
}

