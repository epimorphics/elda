/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        SpecManager.java
    Created by:  Dave Reynolds
    Created on:  7 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.specmanager;

import java.util.List;

import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Defines the interface for a storage manager that can preserve and
 * retrieve specifications and use them to reconstruct routing table
 * entries. The (singleton) instance of the SpecManager will be 
 * associated with a Router and update/delete of specifications
 * will be reflected back to the router.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface SpecManager {
    
    /**
     * Load and record the specification for a new API (which might
     * be associated with multiple APIEndpoints). If the API already exists
     * then it will be updated if the key matches.
     * @param uri The uri for the RDF resource which identifies this API specification.
     * @param key An arbitrary key to identify the provider of the specification. 
     * @param spec An RDF model containing the specification of the API and its 
     * associate endpoints. 
     * @throws APISecurityException if the key does match the key used to create the matching existing entry
     */
    public APISpec addSpec(String prefixPath, AuthMap am, String context, String uri, String key, Model spec) 
    throws APISecurityException;
    
    /**
     * Update the specification for an API.
     * @param uri The uri for the RDF resource which identifies this API specification.
     * @param key An arbitrary key to identify the provider of the specification. 
     * @param spec An RDF model containing the specification of the API and its 
     * associate endpoints. 
     * @throws APISecurityException if the key does match the key used to create the matching existing entry
     */
    public APISpec updateSpec(String prefixPath, AuthMap am, String context, String uri, String key, Model spec)
    throws APISecurityException;
    
    /**
     * Remove the specification for an API.
     * @param uri The uri for the RDF resource which identifies this API specification.
     * @param key An arbitrary key to identify the provider of the specification. 
     * @throws APISecurityException if the key does match the key used to create the matching existing entry
     */
    public void deleteSpec(String context, String uri, String key)
    throws APISecurityException;

    /**
     * Request that the specification which includes an APIEndpoint matching the
     * given request URL be located and loaded into the routing table.
     * This will be called by the router for an unknown incoming request at 
     * enables us to support incremental loading of specifications from 
     * persistent storage.
     * @param uriRequest the incoming request with the server root removed
     */
    public void loadSpecFor(String uriRequest);
    
    /**
     * Return the RDF model which specifies a particular API
     * @param api the URI of the RDF resource identifying the API
     */
    public Model getSpecForAPI(String api);
    
    /**
     * Return the RDF model which specifies the API corresponding to
     * and endpoint matching the given URL. Or null if there is no match.
     */
    public Model getSpecForEndpoint(String url);

    /**
        Answer a list of all the specs in this manager.
    */
	public List<SpecEntry> allSpecs();	
	
	/**
		Get an APISpec object for the given spec
	*/
	public APISpec getAPISpec(Resource specRoot);
}

