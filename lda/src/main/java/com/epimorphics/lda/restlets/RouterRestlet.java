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
import java.util.Map;

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
import com.epimorphics.lda.core.APIEndpointException;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.QueryParseException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererContext;
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
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("{path: .*}") public class RouterRestlet {

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
        return match == null ? null : new Match( match.getValue(), VarValues.uplift( bindings ) );
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
		CallContext cc = CallContext.createContext( ui, bs );
		log.debug("Info: calling APIEndpoint " + ep.getSpec());
		try {
		    Couple<APIResultSet, String> resultsAndFormat = ep.call( cc );
			APIResultSet results = resultsAndFormat.a;
		    if (results == null) {
		        return returnNotFound("No answer back from " + ep.getSpec());
		    } else {
		    	RendererContext rc = new RendererContext( paramsFromContext( cc ), servCon );
		        return renderByType( rc, mediaTypes, pickFormatter( resultsAndFormat.b, suffix ), ep, results );
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

    private Response renderByType( RendererContext rc, List<MediaType> mediaTypes, String rName, APIEndpoint ep, APIResultSet results ) {
    	if (rName == null)
        	{
    		String suppress = rc.getAsString("_supress_media_type", "no");
    		System.err.println( ">> suppress = " + suppress );
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
//       		System.err.println( ">>  " + name + " = " + cc.getParameterValue( name ) );
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

