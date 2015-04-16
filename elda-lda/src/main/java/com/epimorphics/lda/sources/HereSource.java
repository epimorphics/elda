/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.ResourceUtils;

/**
    A Source that pulls its data from the supplied model
    (which typically will be the model that the endpoint resource
    was taken from).
    
 	@author chris
*/
public class HereSource extends SourceBase implements Source
	{
	public static final String PREFIX = "here:";

    static Logger log = LoggerFactory.getLogger( HereSource.class );
    
	final String endpoint;
	final Model model;

	public HereSource( Model fullModel, Resource endpoint ) 
		{
		super( endpoint );
		String endpointString = endpoint.getURI();
        if (!endpointString.startsWith( PREFIX ))
            throw new APIException( "Illegal here endpoint: " + endpointString );
		this.endpoint = endpointString;
		model = ResourceUtils.reachableClosure( fullModel.createResource( endpointString ) );
		model.setNsPrefixes( fullModel );
		}
	
	/**
	    HereSources are in-memory models and support nested selects via ARQ.
	*/
	@Override public boolean supportsNestedSelect() 
		{ return true; }

	@Override public void addMetadata( Resource meta ) 
		{
        meta.addProperty( API.sparqlEndpoint, model.createResource( endpoint ) );
		}

	@Override public QueryExecution execute( Query query ) 
		{
        if (log.isInfoEnabled()) log.info("Creating query:\n" + query);    
        return QueryExecutionFactory.create( query, model );
		}

	@Override public String toString()
		{ return "HereSource{" + endpoint + "}"; }

	@Override public Lock getLock() 
		{ return model.getLock(); }
	}
