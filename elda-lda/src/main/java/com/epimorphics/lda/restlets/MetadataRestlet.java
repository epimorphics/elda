/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.ControlRestlet.lookupRequest;
import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnNotFound;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.restlets.ControlRestlet.SpecRecord;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.ResourceUtils;


/**
 * Support for viewing the metadata associated with an endpoint.
 * This shows a view of the query that is run and the specification
 * that leads to the query but, unlike the control restlet, doesn't
 * directly link to a dynamically updateable specification.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("/meta/{path: .*}") public class MetadataRestlet {
    
    @GET @Produces("text/plain") public Response requestHandlerPlain
    	( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, "text", rec);
            return returnAs(RouterRestlet.NO_EXPIRY, ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/plain");
        }
    }
    
    @GET @Produces("application/rdf+xml")  public Response requestHandlerRDF_XML
    	( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, "rdf", rec);
            return returnAs(RouterRestlet.NO_EXPIRY, ModelIOUtils.renderModelAs(meta.getModel(), "RDF/XML-ABBREV"), "application/rdf+xml");
        }
    }
    
    @GET @Produces("text/turtle") public Response requestHandlerTurtle
    	( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, "ttl", rec);
            return returnAs(RouterRestlet.NO_EXPIRY, ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET @Produces("application/json") public Response requestHandlerJson
    	( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, "json", rec);
            StringWriter writer = new StringWriter();
            List<Resource> roots = new ArrayList<Resource>(1);
            roots.add( meta );
            com.epimorphics.jsonrdf.Context context = rec.getAPIEndpoint().getSpec().getAPISpec().getShortnameService().asContext();
            context.setSorted(true);
            Encoder.getForOneResult( context ).encodeRecursive(meta.getModel(), roots, writer, true);
            String enc = writer.toString();
            return returnAs(RouterRestlet.NO_EXPIRY, enc, "application/json");
        }
    }
    
    @GET @Produces("text/html") public Response requestHandlerHTML
    	( @PathParam("path") String pathstub, @Context ServletContext config, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        String stub = rec == null ? "" : pathstub;
        return new ConfigRestlet().generateConfigPage( stub, config, ui );
    }
    
    @GET public Response requestHandlerAny( @PathParam("path") String pathstub, @Context ServletContext config, @Context UriInfo ui) {
        try {SpecRecord rec = lookupRequest(pathstub, ui);
        String stub = rec == null ? "" : pathstub;
        return new ConfigRestlet().generateConfigPage( stub, config, ui ); }
        catch (RuntimeException e) {
        	System.err.println( "OOPS" );
        	throw new RuntimeException( e );
        }
    }

    static final Property SIBLING = ResourceFactory.createProperty( EXTRAS.NS + "SIBLING" );
    
    private Resource createMetadata(UriInfo ui, String pathStub, String formatName, SpecRecord rec) {
        Bindings cc = Bindings.createContext( Bindings.uplift( rec.getBindings() ), JerseyUtils.convert( ui.getQueryParameters() ) );
        Model metadata = ModelFactory.createDefaultModel();
        Resource meta = rec.getAPIEndpoint().getMetadata( cc, ui.getRequestUri(), formatName, metadata);
    //
        for (APIEndpointSpec s: rec.getAPIEndpoint().getSpec().getAPISpec().getEndpoints()) {
            String ut = s.getURITemplate().replaceFirst( "^/", "" );
            if (!ut.equals(pathStub)) {
                String sib = ui
                    .getRequestUri()
                    .toString()
                    .replace( pathStub, ut )
                    .replace( "%7B", "{" )
                    .replace( "%7D", "}" )
                    ;
                meta.addProperty( SIBLING, metadata.createResource( sib ) );
            }
        }
    // Extract the endpoint specification
        Model spec = rec.getSpecModel();
        Resource endpointSpec = rec.getAPIEndpoint().getSpec().getResource().inModel(spec);
        metadata.setNsPrefixes( spec );
        metadata.add( ResourceUtils.reachableClosure( endpointSpec ) );
        meta.getModel().withDefaultMappings( PrefixMapping.Extended );
        meta.addProperty( API.endpoint, endpointSpec );
        return meta;
    }
}

