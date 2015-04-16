/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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
import java.net.URI;
import java.util.*;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.routing.*;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.tests_support.FileManagerModelLoader;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * A test harness for testing the API code
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APITester {
	
	protected Router router = new DefaultRouter();

    protected Map<String, APISpec> specifications = new HashMap<String, APISpec>();

    public APITester( String specFileName ) {
        this( loadDebuggin(specFileName) );
    }

	private static Model loadDebuggin(String specFileName) {
		return EldaFileManager.get().loadModel(specFileName);
	}

	public APITester( Model model ) {
		this( model, new FileManagerModelLoader() );
	}

	// this API spec disables the metadata options. We might consider accomodating them.
	public APITester( Model model, ModelLoader loader ) {
		for (ResIterator ri = model.listSubjectsWithProperty(RDF.type, API.API); ri.hasNext();) {
            Resource api = ri.next();
            APISpec spec = new APISpec( EldaFileManager.get(), api, loader );
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
        router.register(null, URITemplate, api);
    }
    
    /**
        getMatch looks in the router table for the best match to the given
        path and returns a Match object or null if there's no match at all.
    */
    private Match getMatch( String path ) {
    	MultiMap<String, String> bindings = new MultiMap<String, String>();
    	return router.getMatch(path, bindings);
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
        MultiMap<String, String> map = MakeData.parseQueryString( queryString );
		URI ru = URIUtils.newURI(uriTemplate);
		Bindings call = Bindings.createContext( fix( match.getBindings() ), map );
        return match.getEndpoint().call( new APIEndpoint.Request(controls, ru, call), new NoteBoard()).resultSet;
    }

	static final Controls controls = new Controls( true, new Times() );

	private Bindings fix(Map<String, String> bindings) {
		Bindings result = new Bindings();
		for (String key: bindings.keySet())
			result.put( key, new Value( bindings.get(key) ) );
		return result;
	}

	/**
     * Render  results as JSON, using context mappings from the API
     * (denoted by its localName). Clones the context to avoid corruption.
     */
    public String renderAsJSON(String api, APIResultSet results) {
        StringWriter writer = new StringWriter();
        List<Resource> roots = new ArrayList<Resource>(1);
        roots.add( results.getRoot() );
        Context context = specifications.get(api).getShortnameService().asContext().clone();
        Encoder.get( context ).encodeRecursive(results.getMergedModel(), roots, writer, true);
        return writer.toString();
    }
}

