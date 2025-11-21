/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.sources;

import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.vocabularies.API;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Source that pulls its data from the supplied model
 * (which typically will be the model that the endpoint resource
 * was taken from).
 *
 * @author chris
 */
public class HereSource extends SourceBase implements Source {
    public static final String PREFIX = "here:";

    static Logger log = LoggerFactory.getLogger(HereSource.class);

    final String endpoint;
    final Model model;

    public HereSource(Model fullModel, Resource endpoint) {
        super(endpoint);
        String endpointString = endpoint.getURI();
        if (!endpointString.startsWith(PREFIX))
            throw new APIException("Illegal here endpoint: " + endpointString);
        this.endpoint = endpointString;
        model = ResourceUtils.reachableClosure(fullModel.createResource(endpointString));
        model.setNsPrefixes(fullModel);
    }

    @Override
    public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, model.createResource(endpoint));
    }

    @Override
    public QueryExecution execute(Query query) {
        if (log.isInfoEnabled()) {
            log.info("creating query:\n{}", query);
        }
        return QueryExecutionFactory.create(query, model);
    }

    @Override
    public String toString() {
        return "HereSource{" + endpoint + "}";
    }

    @Override
    public Lock getLock() {
        return model.getLock();
    }
}
