/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        DeploymentRestlet.java
    Created by:  Dave Reynolds
    Created on:  7 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnError;
//import static com.epimorphics.restful.api.RouterRestlet.returnNotFound;

import java.io.StringReader;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APISecurityException;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Support for dynamic configuration of new APIs with associated endpoints
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("/deploy")
public class DeploymentRestlet {

    static Logger log = LoggerFactory.getLogger(DeploymentRestlet.class);
    
    @POST
    public Response deploy(@FormParam("source") String source, @FormParam("key") String key, 
            @FormParam("action") String action, @Context UriInfo ui) {
        try {
            Model spec = ModelFactory.createDefaultModel();
            spec.read( new StringReader(source), "http://localhost/", "Turtle");
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
            return returnAs(hackTogetherSummaryPage(ui.getBaseUri().toASCIIString(), newSpec), "text/html");

        } catch (APISecurityException e)  {
            log.warn("Security exception", e);
            return Response.status(Status.FORBIDDEN)
                .entity("Sorry, that key is not valid for updating this specification")
                .build();
        } catch (Exception e) {
            return returnError(e);
        }
    }
    
    private String hackTogetherSummaryPage(String base, APISpec spec) {
        StringBuilder content = new StringBuilder();
        String description = "Endpoints for " + spec.getSpecURI();
        content.append("<h1>" + description + "</h1>\n");
        content.append("<ul>\n");
        for (APIEndpointSpec eps : spec.getEndpoints()) {
            String url = eps.getURITemplate();
            content.append("  <li>");
            content.append( link_to(url, base + "api" + url) );
            content.append(" [" + link_to("meta", base + "meta" + url) + "]");
            content.append("</li>\n");
        }
        content.append("</ul>\n");
        
        return Util.withBody(description, content.toString());
    }
    
    private String link_to( String label, String uri )
    {
    return " <a href='" + uri + "'>" + safe(label) + "</a>";
    }

    private String safe( String s ) {
        return s.replace( "&", "&amp;" ).replace( "<", "&lt;" );
    }    

    // TODO - add support for REST style PUT/GET/DELETE
}

