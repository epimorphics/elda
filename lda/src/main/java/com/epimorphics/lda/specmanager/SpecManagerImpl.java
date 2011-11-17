/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        SpecManagerImpl.java
    Created by:  Dave Reynolds
    Created on:  7 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.specmanager;

import static com.epimorphics.lda.specmanager.SpecUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIFactory;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

/**
 * Implementation of SpecManager for simple non-GAE environment.
 * This version does not persist the specifications at all beyond
 * the webapp lifetime.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class SpecManagerImpl implements SpecManager {

    static Logger log = LoggerFactory.getLogger(SpecManagerImpl.class);
    
    protected Router router;
    protected ModelLoaderI modelLoader;
    
    protected Map<String, SpecEntry> specs = new HashMap<String, SpecEntry>();
    
    public SpecManagerImpl(Router router, ModelLoaderI modelLoader) {
        this.router = router;
        this.modelLoader = modelLoader;
    }
    
    @Override public APISpec addSpec(String uri, String key, Model spec) throws APISecurityException {
        if (specs.containsKey(uri)) {
            return updateSpec(uri, key, spec);
        } else {
            log.info("Creating API spec at: " + uri);
//            spec.write(System.out, "Turtle");
            APISpec apiSpec = new APISpec( FileManager.get(), spec.getResource(uri), modelLoader);
            synchronized (specs) {
                specs.put(uri, new SpecEntry(uri, key, apiSpec, spec));
            }
            APIFactory.registerApi(router, apiSpec);
            return apiSpec;
        }
    }

    @Override public void deleteSpec(String uri, String key) throws APISecurityException {
        SpecEntry entry = specs.get(uri);
        if (entry == null) {
            // no error if nothing to delete so we can use update safely for create
            return;
        }
        if (! keyMatches(uri, key, entry.keyDigest)) {
            throw new APISecurityException("This key is not permited to modify API " + uri);
        }
        log.info("Delete API sepc: " + uri);
        for (APIEndpointSpec eps : entry.spec.getEndpoints()) {
            router.unregister(eps.getURITemplate());
        }
        synchronized (specs) {
            specs.remove(uri);
        }
    }

    @Override public void loadSpecFor(String uriRequest) {
        // Nothing to do in this environment,  all known specs are permanently loaded
    }

    @Override public APISpec updateSpec(String uri, String key, Model spec) throws APISecurityException {
        log.info("Udating spec: " + uri);
        deleteSpec(uri, key);
        return addSpec(uri, key, spec);
    }

    @Override public Model getSpecForAPI(String api) {
        SpecEntry entry = specs.get(api);
        if (entry != null) 
            return entry.model;
        return null;
    }

    @Override public Model getSpecForEndpoint(String url) {
        Match match = router.getMatch(url);
        if (match != null) {
            String apiURI = match.getEndpoint().getSpec().getAPISpec().getSpecURI();
            return getSpecForAPI(apiURI);
        } 
        return null;
    }

	@Override synchronized public List<SpecEntry> allSpecs() {
		List<SpecEntry> result = new ArrayList<SpecEntry>();
		for (Map.Entry<String, SpecEntry> e: specs.entrySet() ) result.add( e.getValue() );
		return result;
	}

}

