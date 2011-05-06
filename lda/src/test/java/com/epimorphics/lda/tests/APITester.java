/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        APITestSupport.java
    Created by:  Dave Reynolds
    Created on:  5 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.tests;

import java.io.StringWriter;
import java.util.*;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.lda.bindings.Value;
import com.epimorphics.lda.bindings.VarValues;

import com.epimorphics.lda.core.*;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.MultiValuedMapSupport;
import com.epimorphics.lda.tests_support.FileManagerModelLoader;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * A test harness for testing the API code
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APITester {
	
    protected Map<UriTemplate, APIEndpoint> routerTable = new HashMap<UriTemplate, APIEndpoint>();

    protected Map<String, APISpec> specifications = new HashMap<String, APISpec>();

    public APITester( String specFileName ) {
        this( FileManager.get().loadModel(specFileName) );
    }

	public APITester( Model model ) {
		this( model, new FileManagerModelLoader() );
	}

	public APITester( Model model, ModelLoaderI loader ) {
		for (ResIterator ri = model.listSubjectsWithProperty(RDF.type, API.API); ri.hasNext();) {
            Resource api = ri.next();
            APISpec spec = new APISpec(api, loader );
            specifications.put(api.getLocalName(), spec);
            for (APIEndpointSpec eps : spec.getEndpoints()) {
                APIEndpoint ep = APIFactory.makeApiEndpoint(eps);
                register(ep.getURITemplate(), ep);
            }
        }
	}
       
    public void register(String URITemplate, APIEndpoint api) {
        if (URITemplate == null){
            throw new APIException("Tried to register a null endpoint: " + api.getSpec().toString());
        }
        routerTable.put(new UriTemplate(URITemplate), api);
    }
    
    
    /**
        Match bundles up a selected endpoint and the bindings that come with it.
    */
    protected static class Match
        {
        final APIEndpoint endpoint;
        final Map<String, String> bindings;

        public Match( APIEndpoint endpoint, Map<String, String> bindings )
            { this.endpoint = endpoint; this.bindings = bindings; }
        }
    
    /**
        getMatch looks in the router table for the best match to the given
        path and returns a Match object or null if there's no match at all.
        TODO replace with something sensibler.
    */
    private Match getMatch( String path ) {
        int matchlen = 0;
        Map.Entry<UriTemplate, APIEndpoint> match = null;
        Map<String, String> bindings = new HashMap<String, String>();
        for (Map.Entry<UriTemplate, APIEndpoint> e : routerTable.entrySet()) {
            if (e.getKey().match( path, bindings )) {
                int len = e.getValue().getURITemplate().length();
                if (len > matchlen) {
                    matchlen = len;
                    match = e;
                }
            }
        }
        if (match == null) return null; 
        match.getKey().match( path, bindings );
        return new Match( match.getValue(), bindings );
    }
    

    /**
     * Run a test query against the endpoint which equals the given uriTemplate.
     * @param uriTemplate the URI for the endpoint to test, does not support template parameters
     * @param queryString the query string in "param=value&param=value..." format
     * @return the result set from the query
     */
    public APIResultSet runQuery(String uriTemplate, String queryString) {
        Match match = getMatch(uriTemplate);
        if (match == null) 
            throw new APIException("Tester failed to find routed endpoint: " + uriTemplate);
        MultiMap<String, String> map = MultiValuedMapSupport.parseQueryString( queryString );
        // TODO: the template should be a proper URI.
		CallContext call = CallContext.createContext( Util.newURI(uriTemplate), map, fix( match.bindings ) );
        return match.endpoint.call(call).a;
    }

	private VarValues fix(Map<String, String> bindings) {
		VarValues result = new VarValues();
		for (String key: bindings.keySet())
			result.put( key, new Value( bindings.get(key) ) );
		return result;
	}

	/**
     * Render  results as JSON, using context mappings from the API
     * (denoted by its localName).
     */
    public String renderAsJSON(String api, APIResultSet results) {
        StringWriter writer = new StringWriter();
        List<Resource> roots = new ArrayList<Resource>(1);
        roots.add( results.getRoot() );
        Context context = specifications.get(api).getShortnameService().asContext();
        Encoder.get( context ).encodeRecursive(results.getModel(), roots, writer, true);
        return writer.toString();
    }
}

