/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIException;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;

/**
    A Source that pulls its data from the supplied model
    (which typically will be the model that the endpoint resource
    was taken from).
    
 	@author chris
*/
public class HereSource implements Source
	{
	public static final String PREFIX = "here:";

    static Logger log = LoggerFactory.getLogger( HereSource.class );
    
	final String endpoint;
	final Model model;

	public HereSource( Model fullModel, String endpoint ) 
		{
        if (!endpoint.startsWith( PREFIX ))
            throw new APIException( "Illegal here endpoint: " + endpoint );
		this.endpoint = endpoint;
		model = ResourceUtils.reachableClosure( fullModel.createResource( endpoint ) );
		}

	@Override public void addMetadata( Resource meta ) 
		{
        meta.addProperty( API.sparqlEndpoint, model.createResource( endpoint ) );
		}

	@Override public QueryExecution execute( Query query ) 
		{
        if (log.isInfoEnabled()) log.info("Creating query:\n" + query);
        return QueryExecutionFactory.create( query, model );
		}

	public String toString()
		{ return "HereSource{" + endpoint + "}"; }
	}
