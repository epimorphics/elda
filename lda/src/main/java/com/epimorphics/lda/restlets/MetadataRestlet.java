/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        MetadataRestlet.java
    Created by:  Dave Reynolds
    Created on:  14 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.ControlRestlet.lookupRequest;
import static com.epimorphics.lda.restlets.ControlRestlet.renderModelAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnNotFound;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.restlets.ControlRestlet.SpecRecord;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Support for viewing the metadata associated with an endpoint.
 * This shows a view of the query that is run and the specification
 * that leads to the query but, unlike the control restlet, doesn't
 * directly link to a dynamically updateable specification.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("/meta/{path: .*}")
public class MetadataRestlet {
    
    @GET
    @Produces("text/plain")
    public Response requestHandlerPlain(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, rec);
            return returnAs(renderModelAs(meta.getModel(), "Turtle"), "text/plain");
        }
    }
    
    @GET
    @Produces("text/turtle")
    public Response requestHandlerTurtle(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, rec);
            return returnAs(renderModelAs(meta.getModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET
    @Produces("application/json")
    public Response requestHandlerJson(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, rec);
            StringWriter writer = new StringWriter();
            List<Resource> roots = new ArrayList<Resource>(1);
            roots.add( meta );
            com.epimorphics.jsonrdf.Context context = rec.getAPIEndpoint().getSpec().getAPISpec().getShortnameService().asContext();
            context.setSorted(true);
            Encoder.getForOneResult( context ).encodeRecursive(meta.getModel(), roots, writer, true);
            String enc = writer.toString();
            return returnAs(enc, "application/json");
        }
    }

    private Resource createMetadata(UriInfo ui, SpecRecord rec) {
        CallContext cc = CallContext.createContext(ui, rec.getBindings());
        Model metadata = ModelFactory.createDefaultModel();
        Resource meta = rec.getAPIEndpoint().getMetadata( cc , metadata);
        
        // Extract the endpoint specification
        Model spec = rec.getSpecModel();
        Resource endpointSpec = rec.getAPIEndpoint().getSpec().getResource().inModel(spec);
        metadata.add( ResourceUtils.reachableClosure( endpointSpec) );
        meta.addProperty(API.endpoint, endpointSpec);
        return meta;
    }
    
    @GET
    @Produces("text/html")
    public Response requestHandlerHTML(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, rec);
            String body = Util.readResource("textlike/metadescription.html");
            body = replaceByProperty(body, "${comment}", meta, RDFS.comment);
            body = replaceByProperty(body, "${query}", meta, EXTRAS.sparqlQuery);
            body = replaceByProperty(body, "${source}", meta, API.sparqlEndpoint);
            body = body.replace("${specification}", quote( renderModelAs(meta.getModel(), "Turtle") ) );
            body = replaceByProperty(body, "${endpointURL}", meta, EXTRAS.listURL);
            body = replaceByProperty(body, "${endpointName}", meta, EXTRAS.listURL);
            return returnAs(body, "text/html");
        }
    }

    private String replaceByProperty(String body, String pattern, Resource root, Property prop) {
        String val = RDFUtils.getStringValue(root, prop);
        return body.replace(pattern, fullquote(val));
    }
    
    private String fullquote(String val) {
        return quote(val).replaceAll("\\n", "<br />");
    }
    
    private String quote(String val) {
        return val
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;");
    }
}

