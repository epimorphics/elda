/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        RouterServlet.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.QueryParseException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererContext;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.routing.RouterFactory;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaTypes;

/**
 * Handles all incoming API calls and routes to appropriate locations.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
@Path("{path: .*}") public class RouterRestlet {

    static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    
    public RouterRestlet() {
    }
   
    static final Router router = RouterFactory.getDefaultRouter();
    
    public static Match getMatch( String path ) {
        Match match = router.getMatch( path );
        if (match == null) {
            // No match in the table at the moment, but check the persistence
            // manager to see if it can restore an API spec which would enable this endpoint
            // System.err.println( ">> ----------------- " + SpecManagerFactory.get() );
            SpecManagerFactory.get().loadSpecFor(path);
            match = router.getMatch( path );
        }
        return match;
    }
    
    @GET @Produces( { "text/plain", "application/rdf+xml", "application/json", "text/turtle", "text/html", "text/xml" })
    public Response requestHandler(
            @PathParam("path") String pathstub,
            @Context HttpHeaders headers, 
            @Context ServletContext servCon,
            @Context UriInfo ui) throws IOException 
    {
        Couple<String, String> pathAndType = parse( pathstub );
        Match match = getMatch( "/" + pathAndType.a );
        if (match == null) {
            return returnNotFound( "ERROR: Failed to find API handler for path " + ("/" + pathAndType.a) );
        } else {
            List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
            return runEndpoint( servCon, ui, mediaTypes, pathAndType.b, match ); 
        }
    }

    private Response runEndpoint( ServletContext servCon, UriInfo ui, List<MediaType> mediaTypes, String suffix, Match match) {
        APIEndpoint ep = match.getEndpoint();
        VarValues bs = new VarValues( ep.getSpec().getBindings() ).putAll( match.getBindings() );
        CallContext cc = CallContext.createContext( ui.getRequestUri(), JerseyUtils.convert(ui.getQueryParameters()), bs );
        log.debug("Info: calling APIEndpoint " + ep.getSpec());
        try {
            Couple<APIResultSet, String> resultsAndFormat = ep.call( cc );
            APIResultSet results = resultsAndFormat.a;
            if (results == null) {
                return returnNotFound("No answer back from " + ep.getSpec());
            } else {
                RendererContext rc = new RendererContext( paramsFromContext( cc ), servCon );
				String _format = resultsAndFormat.b;
                String formatter = (_format.equals( "" ) ? suffix : _format);
				return renderByType( rc, mediaTypes, formatter, ep, results );
            }
        } catch (EldaException e) {
        	System.err.println( "Caught exception: " + e.getMessage() );
        	e.printStackTrace( System.err );
        	return buildErrorResponse(e);
        } catch (QueryParseException e) {
            e.printStackTrace( System.err );
            return returnNotFound("Failed to parse query request : " + e.getMessage());
        } catch (Throwable e) {
            return returnError(e);
        }
    }

    // TODO this is probably obsolete
    static HashMap<String, MediaType> types = MediaTypes.createMediaExtensions();
    
    //** return (revised path, renderer name or null)
    // TODO work out a spec-conformant method for this lookup
    private Couple<String, String> parse( String pathstub ) 
        {
        String path = pathstub, type = null;
        int dot = pathstub.lastIndexOf( '.' ) + 1;
        if (dot > 0) //  && types.containsKey( pathstub.substring( dot ) )) 
            { path = pathstub.substring(0, dot - 1); type = pathstub.substring(dot); }        
        return new Couple<String, String>( path, type );
        }
    
    private String nameForMimeType( String type )
        {
        for (String name: types.keySet())
            if (types.get(name).toString().equals( type )) return name;
        return null;
        }

    private Response renderByType( RendererContext rc, List<MediaType> mediaTypes, String rName, APIEndpoint ep, APIResultSet results ) {
        if (rName == null)
            {
            String suppress = rc.getAsString("_supress_media_type", "no");
            // System.err.println( ">> suppress = " + suppress );
            if (suppress.equals("no")) {
                for (MediaType mt: mediaTypes) {
                    String type = mt.getType() + "/" + mt.getSubtype();
                    String name = nameForMimeType( type );
                    Renderer renderer = ep.getRendererNamed( name ); 
                    if (renderer != null) 
                        return returnAs( relabel( rc, renderer.render( rc, results ) ), type, results.getContentLocation() );
                }
            }
        //
            RendererFactory rf = ep.getSpec().getRendererFactoryTable().getDefaultFactory();
            ShortnameService sns = ep.getSpec().sns();
            Renderer r = rf.buildWith( ep, sns );
            String mediaType = r.getMediaType();
            return returnAs( relabel( rc, r.render( rc, results ) ), mediaType, results.getContentLocation() );           
            }
        else
            {
            Renderer renderer = ep.getRendererNamed( rName );
            if (renderer == null) {
                String message = "renderer '" + rName + "' is not known to this server.";
                return enableCORS( Response.status( Status.BAD_REQUEST ).entity( message ) ).build();
            } else {
                String type = renderer.getMediaType();
                return returnAs( relabel( rc, renderer.render( rc, results ) ), type, results.getContentLocation() );
            }
        }
    }
    
    // HACK for bootstrapping. Should be endpoint-driven somehow.
    private String relabel( RendererContext rc, String rendered ) {
        String relabel_from = rc.getAsString( "_change_from", null );
        String relabel_to = rc.getAsString( "_change_to", null );
        if (relabel_from == null || relabel_to == null) return rendered;
        return rendered.replace( relabel_from, relabel_to );
    }

    public static VarValues paramsFromContext( CallContext cc ) {
        VarValues result = new VarValues();
           for (Iterator<String> it = cc.parameterNames(); it.hasNext();) {
               String name = it.next();
//               System.err.println( ">>  " + name + " = " + cc.getParameterValue( name ) );
               result.put( name, cc.getStringValue( name ) );
        }
        return result;
    }

    public static ResponseBuilder enableCORS( ResponseBuilder rb ) {
        return rb.header( ACCESS_CONTROL_ALLOW_ORIGIN, "*" );
    }
    
    public static Response returnAs(String response, String mimetype) {
        return enableCORS( Response.ok(response, mimetype) ).build();
    }
    
    public static Response returnAs(String response, String mimetype, String contentLocation) {
        try {
            return enableCORS( Response.ok(response, mimetype) )
                    // .contentLocation( new URI(contentLocation) ) // what does it do & how can we get it back 
                    .build();
        } catch (RuntimeException e) { // (URISyntaxException e) {
            return returnError(e);
        }
    }

    public static Response returnError(Throwable e) {
        log.error("Exception: " + e.getMessage(), e);
        return enableCORS( Response.serverError() ).entity( e.getMessage() ).build();
    }
    
    public static Response returnError(String s) {
        log.error("Exception: " + s );
        return enableCORS( Response.serverError() ).entity( s ).build();
    }
    
    public static Response returnNotFound( String message ) {
        log.warn( "Failed to return results: " + message );
        new RuntimeException("returning NotFound: '" + message + "'").printStackTrace( System.err );
        return enableCORS( Response.status(Status.NOT_FOUND) ).entity(message).build();
    }
	private Response buildErrorResponse( EldaException e ) {
		return enableCORS( Response.status(e.code) )
			.entity( niceMessage( e ) )
			.build()
			;
	}
    
   private String niceMessage( EldaException e ) {
		return
			"<html>"
			+ "\n<head>"
			+ "\n<title>alas</title>"
			+ "\n</head>"
			+ "\n<body style='background-color: #ffdddd'>"
			+ "\n<h2>there seems to be a problem.</h2>"
			+ "\n<p>" + e.getMessage() + "</p>"
			+ (e.moreMessage == null ? "" : "<p>" + e.moreMessage + "</p>")
			+ "\n</body>"
			+ "\n</html>"
			+ "\n"
			;
	}
}

