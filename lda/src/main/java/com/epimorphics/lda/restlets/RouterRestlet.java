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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.URLforResource;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIEndpointUtil;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.exceptions.ExpansionFailedException;
import com.epimorphics.lda.exceptions.QueryParseException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.routing.RouterFactory;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.Controls;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.support.pageComposition.Messages;
import com.epimorphics.lda.support.statistics.StatsValues;
import com.epimorphics.util.Couple;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.Triad;
import com.hp.hpl.jena.shared.WrappedException;

/**
 * Handles all incoming API calls and routes to appropriate locations.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
@Path("{path: .*}") public class RouterRestlet {

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

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
    	MultivaluedMap<String, String> rh = headers.getRequestHeaders();
    	boolean dontCache = has( rh, "pragma", "no-cache" ) || has( rh, "cache-control", "no-cache" );
        Couple<String, String> pathAndType = parse( pathstub );
        Match matchAll = getMatch( "/" + pathstub );
        Match matchTrimmed = getMatch( "/" + pathAndType.a );
        Match match = matchTrimmed == null || notFormat( matchTrimmed, pathAndType.b ) ? matchAll : matchTrimmed;
        String type = match == matchAll ? null : pathAndType.b;
        if (match == null) {
        	StatsValues.endpointNoMatch();
        	return noMatchFound( pathstub, ui, pathAndType );
        } else {
        	Times t = new Times( pathstub );
        	Controls c = new Controls( !dontCache, t );
            List<MediaType> mediaTypes = getAcceptableMediaTypes( headers );
            Response r = runEndpoint( c, servCon, ui, mediaTypes, type, match );
            StatsValues.accumulate( t.done() );
			return r; 
        }
    }
    
    private boolean has( MultivaluedMap<String, String> rh, String key, String value ) {
    	List<String> values = rh.get( key );
		return values != null && values.contains( value );
	}

	protected static final boolean showMightHaveMeant = false;

	private Response noMatchFound( String pathstub, UriInfo ui, Couple<String, String> pathAndType ) {
		String preamble = ui.getBaseUri().toASCIIString();
		String message = "Could not find anything matching " + ("/" + pathstub);
		if (pathAndType.b != null) message += " (perhaps '" + pathAndType.b + "' is an incorrect format name?)";
		if (showMightHaveMeant) {
			message += "<div style='margin-bottom: 2px'>you might have meant any of:</div>\n";
			for (String template: reversed(router.templates())) {
				message += "\n<div style='margin-left: 2ex'>" + maybeLink(preamble, template) + "</div>";
			}
		}
		return returnNotFound( message + "\n", "/" + pathstub );
	}
    
    private String maybeLink( String preamble, String template ) {
    	if (template.contains( "{")) return template;
    	return "<a href='" + preamble + template.substring(1) + "'>" + template + "</a>";
    }

	private List<String> reversed( List<String> x ) {
    	int size = x.size(), limit = size / 2;
    	for (int i = 0, j = size; i < limit; i += 1) {
    		String temp = x.get(i);
    		x.set(i, x.get(--j) );
    		x.set( j, temp );
    	}
    	return x;
	}

	/**
        Answer true of m's endpoint has no formatter called type.
    */
    private boolean notFormat( Match m, String type ) {
    	return m.getEndpoint().getRendererNamed( type ) == null;
	}
    
	//** return (revised path, renderer name or null)
    // TODO work out a spec-conformant method for this lookup
    public static Couple<String, String> parse( String pathstub ) 
        {
        String path = pathstub, type = null;
        int dot = pathstub.lastIndexOf( '.' ) + 1;
        int slash = pathstub.lastIndexOf( '/' );
        if (dot > 0 && dot > slash) 
            { path = pathstub.substring(0, dot - 1); type = pathstub.substring(dot); }        
        return new Couple<String, String>( path, type );
        }

    /**
     	Translate the Jersey media types into Elda media types (because there will
     	be Jersey-less versions of Elda). Also, if text/html is present, prefer it
     	regardless of the given order, for those browsers still out there that
     	"prefer" XML to HTML. They may, but their readers don't.
    */
	private List<MediaType> getAcceptableMediaTypes(HttpHeaders headers) {
		boolean preferHTML = false;
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		for (javax.ws.rs.core.MediaType mt: headers.getAcceptableMediaTypes()) {
			MediaType newMT = new MediaType( mt.getType(), mt.getSubtype() );
			if (newMT.equals( MediaType.TEXT_HTML)) preferHTML = true;
			else mediaTypes.add( newMT );
		}
		if (preferHTML) mediaTypes.add( 0, MediaType.TEXT_HTML );
		return mediaTypes;
	}

    private Response runEndpoint( Controls c, ServletContext servCon, UriInfo ui, List<MediaType> mediaTypes, String suffix, Match match) {
    	URLforResource as = pathAsURLFactory(servCon);
    	URI requestUri = ui.getRequestUri();
    	MultiMap<String, String> queryParams = JerseyUtils.convert(ui.getQueryParameters());
//
        try {
        	URI ru = makeRequestURI(ui, match, requestUri);
        	Triad<APIResultSet, String, Bindings> resultsAndFormat = APIEndpointUtil.call( c, match, ru, suffix, queryParams );
            APIResultSet results = resultsAndFormat.a;
            if (results == null)
            	throw new RuntimeException( "ResultSet is null -- this should never happen." );
            APIEndpoint ep = match.getEndpoint();
			Bindings rc = new Bindings( resultsAndFormat.c.copy(), as );
			String _format = resultsAndFormat.b;
			String formatter = (_format.equals( "" ) ? suffix : resultsAndFormat.b);
			Renderer r = APIEndpointUtil.getRenderer( ep, formatter, mediaTypes );
			return doRendering( c, rc, formatter, results, r );
        } catch (StackOverflowError e) {
        	StatsValues.endpointException();
            log.error("Stack Overflow Error" );
            if (log.isDebugEnabled()) log.debug( Messages.shortStackTrace( e ) );
            return enableCORS( Response.serverError() ).entity( e.getMessage() ).build();
        } catch (ExpansionFailedException e) {
        	StatsValues.endpointException();
        	return buildErrorResponse(e);
        } catch (EldaException e) {
        	StatsValues.endpointException();
        	log.error( "Exception: " + e.getMessage() );
        	if (log.isDebugEnabled())log.debug( Messages.shortStackTrace( e ) );
        	return buildErrorResponse(e);
        } catch (QueryParseException e) {
        	StatsValues.endpointException();
            log.error( "Query Parse Exception: " + e.getMessage() );
            if (log.isDebugEnabled())log.debug( Messages.shortStackTrace( e ) );
            return returnNotFound("Failed to parse query request : " + e.getMessage());
        } catch (Throwable e) {
        	StatsValues.endpointException();
            return returnError( e );
        }
    }    

	private URI makeRequestURI(UriInfo ui, Match match, URI requestUri) throws URISyntaxException {
		String base = match.getEndpoint().getSpec().getAPISpec().getBase();
		if (base == null) return requestUri;
		URI baseAsURI = new URI( base );
		URI resolved = baseAsURI.isAbsolute() 
			? baseAsURI.resolve( ui.getPath() ) 
			: requestUri.resolve( base ).resolve( ui.getPath() )
			;
		URI result = new URI(
			resolved.getScheme(),
			resolved.getUserInfo(),
			resolved.getHost(),
			resolved.getPort(),
			resolved.getPath(),
			requestUri.getQuery(),
			resolved.getFragment()
		);		
//		System.err.println( ">> manufactured request URI = '" + result + "'" );
		return result;
	}

	private static URLforResource pathAsURLFactory( final ServletContext servCon ) {
		return new URLforResource() 
			{
			@Override public URL asResourceURL( String ePath ) { 		
			String p = ePath.startsWith( "/" ) || ePath.startsWith( "http://") ? ePath : "/" + ePath;
			try {
				URL result = servCon.getResource( p );
				if (result == null) EldaException.NotFound( "webapp resource", ePath );
				return result;
				}
			catch (MalformedURLException e) 
				{
				throw new WrappedException( e );
				} 
			}
		};
	}
	
    private Response doRendering( Controls c, Bindings rc, String rName, APIResultSet results, Renderer r ) {
		if (r == null) {
            String message = rName == null
            	? "no suitable media type was provided for rendering."
            	: "renderer '" + rName + "' is not known to this server."
            	;
            return enableCORS( Response.status( Status.BAD_REQUEST ).entity( Messages.niceMessage( message ) ) ).build();
        } else {
            MediaType mt = r.getMediaType( rc );
            long base = System.currentTimeMillis();
            String rendering = r.render( rc, results );
            c.times.setRenderedSize( rendering.length() * 2 );
            c.times.setRenderDuration( System.currentTimeMillis() - base, (rName == null ? r.getMediaType(rc).toString() : rName) );
			return returnAs( rendering, mt, results.getContentLocation() );
        }
	}

    public static ResponseBuilder enableCORS( ResponseBuilder rb ) {
        return rb.header( ACCESS_CONTROL_ALLOW_ORIGIN, "*" );
    }
    
    public static Response returnAs(String response, String mimetype) {
        return enableCORS( Response.ok(response, mimetype) ).build();
    }
    
    public static Response returnAs(String response, MediaType mimetype, String contentLocation) {
        try {
            return enableCORS( Response.ok(response, mimetype.toString()) )
                    // .contentLocation( new URI(contentLocation) ) // what does it do & how can we get it back 
                    .build();
        } catch (RuntimeException e) { // (URISyntaxException e) {
            return returnError(e);
        }
    }

    public static Response returnError( Throwable e ) {
        String shortMessage = e.getMessage();
		String longMessage = Messages.niceMessage( shortMessage, "Internal Server error." );
		log.error("Exception: " + shortMessage );
        log.debug( Messages.shortStackTrace( e ) );
        return enableCORS( Response.serverError() ).entity( longMessage ).build();
    }

	public static Response returnError( String s ) {
        log.error("Exception: " + s );
        return enableCORS( Response.serverError() ).entity( s ).build();
    }

    public static Response returnNotFound( String message ) {
    	return returnNotFound( message, "" );
    }
    
    public static Response returnNotFound( String message, String what ) {
        log.debug( "Failed to return results: " + Messages.brief( message ) );
        return enableCORS( Response.status(Status.NOT_FOUND) ).entity( Messages.niceMessage( message, "404 Resource Not Found: " + what ) ).build();
    }
    
	private Response buildErrorResponse( EldaException e ) {
		return enableCORS( Response.status( e.code ) )
			.entity( Messages.niceMessage( e ) )
			.build()
			;
	}
}

