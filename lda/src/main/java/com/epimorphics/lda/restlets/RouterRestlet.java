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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.BindingSet;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointException;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.QueryParseException;
import com.epimorphics.lda.renderers.JSONRenderer;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererFactory;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaTypes;
import com.hp.hpl.jena.shared.NotFoundException;
import com.sun.jersey.api.uri.UriTemplate;

/**
 * Handles all incoming API calls and routes to appropriate locations.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("{path: .*}")
public class RouterRestlet {

    static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    // Real router needs to cater for overlapping patterns and pick most specific
    protected static Map<UriTemplate, APIEndpoint> routerTable = new HashMap<UriTemplate, APIEndpoint>();
    
    static public void register(String URITemplate, APIEndpoint api) {
        log.info("Registering api " + api.getSpec() + " at " + URITemplate);
        synchronized (routerTable) {
            routerTable.put(new UriTemplate(URITemplate), api);
        }
    }

    
    public RouterRestlet() {
    }

    static public void unregister(String URITemplate) {
        synchronized (routerTable) {
            Iterator<Map.Entry<UriTemplate, APIEndpoint>> i = routerTable.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<UriTemplate, APIEndpoint> e = i.next();
                if (e.getValue().getURITemplate().equals(URITemplate)) {
                    log.info("Removing API registered at " + URITemplate);
                    i.remove();
                    return;
                }
            }
        }
        log.error("Failed to unregister " + URITemplate);
    }
    
    /**
        getMatch looks in the router table for the best match to the given
        path and returns a Match object or null if there's no match at all.
        TODO replace with something sensibler.
    */
    public static Match getMatch( String path ) {
        Match match = tryMatch(path);
        if (match == null) {
            // No match in the table at the moment, but check the persistence
            // manager to see if it can restore an API spec which would enable this endpoint
        	// System.err.println( ">> ----------------- " + SpecManagerFactory.get() );
            SpecManagerFactory.get().loadSpecFor(path);
            match = tryMatch(path);
        }
        return match;
    }

    private static Match tryMatch( String path ) {
    	log.debug( "tryMatch: " + path );
        int matchlen = 0;
        Map.Entry<UriTemplate, APIEndpoint> match = null;
        Map<String, String> bindings = new HashMap<String, String>();
        synchronized (routerTable) {
            for (Map.Entry<UriTemplate, APIEndpoint> e : routerTable.entrySet()) {
            	log.debug( "considering entry: " + e );
//            	System.err.println( "||  considering entry: " + e );
                if (e.getKey().match( path, bindings )) {
                    int len = e.getValue().getURITemplate().length();
                    if (len > matchlen) {
                        matchlen = len;
                        match = e;
                    }
                }
            }
        }
        return match == null ? null : new Match( match.getValue(), BindingSet.uplift( bindings ) );
    }
    
    @GET @Produces( { "text/plain", "application/rdf+xml", "application/json", "text/turtle", "text/html", "text/xml" })
    public Response requestHandler(
            @PathParam("path") String pathstub,
            @Context HttpHeaders headers, 
            @Context UriInfo ui) 
    {
        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
//        String mediaSuffix = getMediaTypeSuffix( headers );
        Couple<String, String> pathAndType = parse( pathstub );
        System.err.println( ">> pathAndType = " + pathAndType );
        String path = "/" + pathAndType.a;
        Match match = getMatch( path );

        if (match == null) {
            return returnNotFound("ERROR: Failed to find API handler for path " + path);
        } else {
            CallContext cc = CallContext.createContext( ui, match.getBindings() );
            APIEndpoint ep = match.getEndpoint();
            log.debug("Info: calling APIEndpoint " + ep.getSpec());

            try {
                Couple<APIResultSet, String> resultsAndFormat = ep.call( cc );
				APIResultSet results = resultsAndFormat.a;
                if (results == null) {
                    return returnNotFound("No answer back from " + ep.getSpec());
                } else {
                    return renderByType( mediaTypes, pickFormatter( resultsAndFormat.b, pathAndType.b ), ep, results );
                }
            } catch (NotFoundException e) { // TODO echeck that it's VIEW not found.
            	return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
            } catch (APIEndpointException e) {
                return returnNotFound("Query Failed.\n" + e.getMessage());
            } catch (QueryParseException e) {
            	e.printStackTrace( System.err );
                return returnNotFound("Failed to parse query request : " + e.getMessage());
            } catch (Throwable e) {
                return returnError(e);
            } 
        }
    }
    
    
   private String pickFormatter( String _format, String dotSuffix ) {
		return _format.equals( "" ) ? dotSuffix : _format;
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

    private Response renderByType( List<MediaType> mediaTypes, String rName, APIEndpoint ep, APIResultSet results ) {
        if (rName == null)
        	{
        	for (MediaType mt: mediaTypes) {
        		String type = mt.getType() + "/" + mt.getSubtype();
        		String name = nameForMimeType( type );
        		Renderer renderer = ep.getRendererNamed( name ); // ep.getRendererFor(type);
        		if (renderer != null) return returnAs(renderer.render(results).toString(), type, 
        				results.getContentLocation());
        	}
        //
        	RendererFactory rf = ep.getSpec().getRendererFactoryTable().getDefaultFactory();
        	ShortnameService sns = ep.getSpec().sns();
        	Renderer r = rf.buildWith( ep, sns );
        	String mediaType = r.getMimeType();
        	return returnAs( r.render( results ).toString(), mediaType, results.getContentLocation() );
//        	String choices = MediaTypeSupport.mediaTypeString( mediaTypes );
//        	log.warn( "looks like no known media type was specified [choices: " + choices + "], using text/plain." );
//        	return renderAsPlainText( results, ep );        	
        	}
        else
        	{
        	Renderer renderer = ep.getRendererNamed( rName );
        	if (renderer == null) {
        		String message = "renderer '" + rName + "' is not known to this server.";
        		return enableCORS( Response.status( Status.BAD_REQUEST ).entity( message ) ).build();
        	} else {
        		String type = renderer.getMimeType();
	        	return returnAs( renderer.render(results).toString(), type, results.getContentLocation() );
    		}
        }
    }

	public static Response renderAsPlainText( APIResultSet results, APIEndpoint ep ) {
        return returnAs( new JSONRenderer(ep).render(results), "text/plain" );
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
                    .contentLocation( new URI(contentLocation) )
                    .build();
        } catch (URISyntaxException e) {
            return returnError(e);
        }
    }
    
    public static Response returnError(Throwable e) {
        log.error("Exception: " + e.getMessage(), e);
        return enableCORS( Response.serverError() ).entity( e.getMessage() ).build();
    }
    
    public static Response returnError(String message) {
        log.error( message );
        new RuntimeException( "returning error: '" + message + "'" ).printStackTrace( System.err );
        return enableCORS( Response.serverError() ).entity(message).build();
    }
    
    public static Response returnNotFound(String message) {
        log.warn("Failed to return results: " + message);
        new RuntimeException("returning NotFound: '" + message + "'").printStackTrace( System.err );
        return enableCORS( Response.status(Status.NOT_FOUND) ).entity(message).build();
    }
}

