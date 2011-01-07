/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        MetadataRestlet.java
    Created by:  Dave Reynolds
    Created on:  8 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnError;
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

import org.openjena.atlas.json.JsonException;

import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.lda.bindings.BindingSet;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointSpec;
import com.epimorphics.lda.core.APIException;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.routing.RouterFactory;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Supports REST access to the definition of an API endpoint.
 * For each API there are real data URLs which returns pages
 * of content of the defined List/Set:
 * <pre>   http://...proxy/api/doc/schools</pre>
 * corresponding metdata URLs which returns the specification of the
 * endpoint:
 * <pre>   http://...proxy /meta/doc/schools</pre>
 * and a single API level metadata URL which returns the specification of
 * the API and, in an HTML rendering, a form to allow update.
 * <p>
 * This result implements the /meta URLs
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("/control/{path: .*}")
public class ControlRestlet {

    static class SpecRecord {
        final Match match;
        final boolean isAPI;   

        private Model spec;
        private Resource apiRoot;
        
        SpecRecord(Match match, boolean isAPI) {
            this.match = match;
            this.isAPI = isAPI;
        }
        
        public Model getSpecModel() {
            if (spec == null) {
                APIEndpointSpec eps = match.getEndpoint().getSpec();
                String api = eps.getAPISpec().getSpecURI();
                spec = SpecManagerFactory.get().getSpecForAPI(api);
                if (spec != null)
                    apiRoot = spec.createResource( api );
            }
            return spec;
        }
        
        public Resource getApiRoot() {
            getSpecModel();
            return apiRoot;
        }
        
        public APIEndpoint getAPIEndpoint() {
            return match.getEndpoint();
        }
        
     // false for an individual endpoint
        public boolean isAPI() {
            return isAPI;
        }
        
        public BindingSet getBindings() {
            return match.getBindings();
        }
    }
    
    @GET @Produces("text/plain") public Response requestHandlerPlain(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            return returnAs(renderModelAs(rec.getSpecModel(), "Turtle"), "text/plain");
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
            return returnAs(renderModelAs(rec.getSpecModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET @Produces("application/rdf+xml") public Response requestHandlerRDF(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            return returnAs(renderModelAs(rec.getSpecModel(), "RDF/XML-ABBREV"), "application/rdf+xml");
        }
    }
    
    @GET @Produces("application/json") public Response requestHandlerJSON(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            String enc;
            try {
                List<Resource> roots = new ArrayList<Resource>();
                roots.add( rec.getApiRoot());
                enc = Encoder.getForOneResult().encodeRecursive(rec.getSpecModel(), roots).toString();
            } catch (JsonException e) {
                return returnError(e);
            } catch (NotFoundException e) { // TODO ensure this is clean
            	return returnNotFound( e.getMessage() );
            }
            return returnAs(enc, "application/json");
        }
    }
    
    @GET @Produces("text/html") public Response requestHandlerHTML(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            // TODO replace by sensible template renderer
            String body = Util.readResource("textlike/spec-form.html")
                .replace("${endpoint}", pathstub)
                .replace("${list}", ui.getBaseUri() + "api/" + pathstub)
                .replace("${model}", renderModelAs(rec.getSpecModel(), "Turtle"));
            return returnAs(body, "text/html");
        }
    }

    public static String renderModelAs(Model model, String format) {
        StringWriter writer = new StringWriter();
        model.write(writer, format);
        return writer.toString();
    }
    
    public static SpecRecord lookupRequest(String pathstub, UriInfo ui) {
        String path = "/" + pathstub;
        Match match = RouterFactory.get().getMatch(path);
        if (match == null)  {
            return null;        
        } else {
            return new SpecRecord(match, false);
        }
    }

    /**
     *  Annotate the endpoints in the model with cross links to the corresponding proxy endpoints
     */
    @SuppressWarnings("unused")	private void annotateEndpoints(Model spec, String baseURI) {
        NodeIterator ri = spec.listObjectsOfProperty(API.endpoint);
        List<Statement> toadd = new ArrayList<Statement>();
        while (ri.hasNext()) {
            RDFNode viewN = ri.next();
            if ( ! (viewN instanceof Resource)) {
                throw new APIException("Bad specification file, non-resource defintion of Endpoint. " + viewN);
            }
            Resource view = (Resource)viewN;
            ExtendedIterator<Statement> si = view.listProperties(API.uriTemplate)
                                                 ; // .andThen(view.listProperties(API.uri));
            while (si.hasNext()) {
                String uri = RDFUtils.getLexicalForm(si.next().getObject());
                toadd.add( spec.createStatement(view, EXTRAS.listURL, spec.createResource(baseURI + "api" + uri)));
                toadd.add( spec.createStatement(view, EXTRAS.metaURL, spec.createResource(baseURI + "meta" + uri)));
            }
        }
        spec.add(toadd);
    }
    
}

