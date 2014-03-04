/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.RouterRestlet.*;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.jena.atlas.json.JsonException;

import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.routing.*;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.rdf.model.*;
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

@Path("/control/{path: .*}") public class ControlRestlet {

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
        
        public Map<String, String> getBindings() {
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
            return returnAs(RouterRestlet.NO_EXPIRY, ModelIOUtils.renderModelAs(rec.getSpecModel(), "Turtle"), "text/plain");
        }
    }
    
    @GET @Produces("text/turtle") public Response requestHandlerTurtle(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            return returnAs(RouterRestlet.NO_EXPIRY, ModelIOUtils.renderModelAs(rec.getSpecModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET @Produces("application/rdf+xml") public Response requestHandlerRDF(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            return returnAs(RouterRestlet.NO_EXPIRY, ModelIOUtils.renderModelAs(rec.getSpecModel(), "RDF/XML-ABBREV"), "application/rdf+xml");
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
            } catch (Throwable e) {
            	return returnError( e.getMessage() );
            }
            return returnAs(RouterRestlet.NO_EXPIRY, enc, "application/json");
        }
    }
    
    @GET @Produces("text/html") public Response requestHandlerHTML(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            // TODO replace by sensible template renderer (Issue 53)
            String body = Util.readResource("textlike/spec-form.html")
                .replace("${endpoint}", pathstub)
                .replace("${list}", ui.getBaseUri() + "api/" + pathstub)
                .replace("${model}", ModelIOUtils.renderModelAs(rec.getSpecModel(), "Turtle"));
            return returnAs(RouterRestlet.NO_EXPIRY, body, "text/html");
        }
    }
    
    public static SpecRecord lookupRequest(String pathstub, UriInfo ui) {
        String path = "/" + pathstub;
        Router r = RouterFactory.getDefaultRouter();
		MultiMap<String, String> params = new MultiMap<String, String>();
		Match match = r.getMatch(path, params );
        if (match == null) match = r.getMatch(trimmed(path), params);
        if (match == null)  {
            return null;        
        } else {
            return new SpecRecord(match, false);
        }
    }

    private static String trimmed( String path ) {
        int dot = path.lastIndexOf( '.' ) + 1;
        int slash = path.lastIndexOf( '/' );
        if (dot > 0 && dot > slash) return path.substring(0, dot - 1); 
		return path;
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
                throw new APIException("Bad specification file, non-resource definition of Endpoint. " + viewN);
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

