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
import com.epimorphics.lda.restlets.RouterRestlet;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.*;
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
        String seqID = RouterRestlet.getSeqID();
    //
        if (ep != null) {
        	allowInsecure = RDFUtils.getBooleanValue(ep, ELDA_API.authAllowInsecure, false);
        //
        	String authKey = RDFUtils.getStringValue( ep, ELDA_API.authKey, null );
        	if (authKey != null) {
//        		System.err.println(">> authKey: " + authKey);
//        		System.err.println(">> authMap: " + am);
				log.debug(String.format("[%s]: handling auth key '%s'", seqID, authKey));
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
        log.info(String.format( "[%s]: created '%s'", seqID, this.toString() ));
    }
    
    @Override public QueryExecution execute(Query query) {
        String seqID = RouterRestlet.getSeqID();
        if (log.isDebugEnabled()) log.debug(String.format("[%s]: running query on '%s':\n%s", seqID, sparqlEndpoint, query));
		QueryEngineHTTP qe = new QueryEngineHTTP(sparqlEndpoint, query);
		if (basicUser != null) {
			log.debug(String.format( "[%s]: basic user '%s'", seqID, basicUser));			
			log.debug(String.format( "[%s]: basic password '%s'", seqID, new String(basicPassword)));
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

