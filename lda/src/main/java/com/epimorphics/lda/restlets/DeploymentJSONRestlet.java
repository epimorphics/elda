/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        DeploymentJSON.java
    Created by:  Dave Reynolds
    Created on:  21 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnError;

import java.io.StringReader;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.jsonrdf.Decoder;
import com.epimorphics.lda.core.APISpec;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

@Path("/deployJSON") public class DeploymentJSONRestlet {

    static Logger log = LoggerFactory.getLogger(DeploymentJSONRestlet.class);
    
    static com.epimorphics.jsonrdf.Context apiContext
        = new com.epimorphics.jsonrdf.Context(
                Util.readModel("apiconfig/apicontext.ttl") );
            
    @POST public Response deploy(@FormParam("source") String source, 
            @FormParam("action") String action, @Context UriInfo ui) {
        try {
            List<Resource> roots = Decoder.decode(apiContext, new StringReader(source));
            if (roots.isEmpty()) {
                return returnError("No content in request");
            }
            Model spec = roots.get(0).getModel();
            String key = "key";             // Dummy key, no security in current demo version

            ResIterator ri = spec.listSubjectsWithProperty(RDF.type, API.API);
            if (!ri.hasNext()) {
                return returnError("No specification found");
            }
            Resource api = ri.next();
            APISpec newSpec = null;
            if (action.equals("Create")) {
                newSpec = SpecManagerFactory.get().addSpec(api.getURI(), key, spec);
            } else if (action.equals("Update")) {
                newSpec = SpecManagerFactory.get().updateSpec(api.getURI(), key, spec);
            } else if (action.equals("Delete")) {
                SpecManagerFactory.get().deleteSpec(api.getURI(), key);
                return returnAs("Delete API " + api, "text/plain");
            } else {
                return returnError("Didn't recognize action request " + action);
            }
            return returnAs( "sdx/api" + newSpec.getEndpoints().get(0).getURITemplate(), "text/plain");

        } catch (Exception e) {
            return returnError(e);
        }
    }
    
}

