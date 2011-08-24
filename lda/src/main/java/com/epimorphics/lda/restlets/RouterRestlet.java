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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.APIEndpointUtil;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.lda.core.QueryParseException;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererContext;
import com.epimorphics.lda.renderers.RendererContext.AsURL;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.routing.RouterFactory;
import com.epimorphics.lda.shortnames.ExpansionFailedException;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
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
        Couple<String, String> pathAndType = parse( pathstub );
        Match matchAll = getMatch( "/" + pathstub );
        Match matchTrimmed = getMatch( "/" + pathAndType.a );
        Match match = matchTrimmed == null || notFormat( matchTrimmed, pathAndType.b ) ? matchAll : matchTrimmed;
        String type = match == matchAll ? null : pathAndType.b;
        if (match == null) {
        	return noMatchFound( pathstub, ui, pathAndType );
        } else {
            List<MediaType> mediaTypes = getAcceptableMediaTypes( headers );
            return runEndpoint( servCon, ui, mediaTypes, type, match ); 
        }
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

	private List<MediaType> getAcceptableMediaTypes(HttpHeaders headers) {
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		for (javax.ws.rs.core.MediaType mt: headers.getAcceptableMediaTypes())
			mediaTypes.add( new MediaType( mt.getType(), mt.getSubtype() ) );
		return mediaTypes;
	}

    private Response runEndpoint( ServletContext servCon, UriInfo ui, List<MediaType> mediaTypes, String suffix, Match match) {
    	RendererContext.AsURL as = pathAsURLFactory(servCon);
    	String contextPath = servCon.getContextPath();
    	URI requestUri = ui.getRequestUri();
    	MultiMap<String, String> queryParams = JerseyUtils.convert(ui.getQueryParameters());
//
        try {
        	URI ru = makeRequestURI(ui, match, requestUri);
        	Triad<APIResultSet, String, CallContext> resultsAndFormat = APIEndpointUtil.call( match, ru, suffix, queryParams );
            APIResultSet results = resultsAndFormat.a;
			if (false) { // results == null || results.getResultList().size() == 0) {
			    return returnNotFound( "No items found matching that request." );
			} else {
				// APIEndpoint ep = match.getEndpoint();
				RendererContext rc = new RendererContext( paramsFromContext( resultsAndFormat.c ), contextPath, as );
				String _format = resultsAndFormat.b;
				String formatter = (_format.equals( "" ) ? suffix : resultsAndFormat.b);
				Renderer r = APIEndpointUtil.getRenderer( match.getEndpoint(), formatter, mediaTypes );
				return doRendering( rc, formatter, results, r );
			}
        } catch (StackOverflowError e) {
            log.error("Stack Overflow Error" );
            if (log.isDebugEnabled()) log.debug( shortStackTrace( e ) );
            return enableCORS( Response.serverError() ).entity( e.getMessage() ).build();
        } catch (ExpansionFailedException e) {
        	return buildErrorResponse(e);
        } catch (EldaException e) {
        	log.error( "Exception: " + e.getMessage() );
        	if (log.isDebugEnabled())log.debug( shortStackTrace( e ) );
        	return buildErrorResponse(e);
        } catch (QueryParseException e) {
            log.error( "Query Parse Exception: " + e.getMessage() );
            if (log.isDebugEnabled())log.debug( shortStackTrace( e ) );
            return returnNotFound("Failed to parse query request : " + e.getMessage());
        } catch (Throwable e) {
            return returnError( e );
        }
    }    

	private static String shortStackTrace( Throwable e ) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( bos );
		e.printStackTrace( ps );
		ps.flush();
		return shorten( bos.toString() );
	}
	
	private static String shorten(String l) {
		int len = l.length();
		if (len < 1000) return l;
		return l.substring(0, 300) + "\n...\n" + l.substring(len - 700, len - 1);
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

	private static AsURL pathAsURLFactory(final ServletContext servCon) {
		return new RendererContext.AsURL() 
			{@Override public URL asResourceURL( String p ) 
			{ try {
				URL result = servCon.getResource( p );
				if (result == null) EldaException.NotFound( "webapp resource", p );
				return result;
			} catch (MalformedURLException e) {
				throw new WrappedException( e );
			} }
		};
	}
	
    private Response doRendering( RendererContext rc, String rName, APIResultSet results, Renderer r ) {
		if (r == null) {
            String message = rName == null
            	? "no suitable media type was provided for rendering."
            	: "renderer '" + rName + "' is not known to this server."
            	;
            return enableCORS( Response.status( Status.BAD_REQUEST ).entity( niceMessage( message ) ) ).build();
        } else {
            MediaType mt = r.getMediaType( rc );
            return returnAs( r.render( rc, results ), mt, results.getContentLocation() );
        }
	}
    
    public static VarValues paramsFromContext( CallContext cc ) {
        VarValues result = new VarValues();
        for (Iterator<String> it = cc.parameterNames(); it.hasNext();) {
            String name = it.next();
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
		String longMessage = niceMessage( shortMessage, "Internal Server error." );
		log.error("Exception: " + shortMessage );
        log.debug( shortStackTrace( e ) );
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
        log.debug( "Failed to return results: " + brief( message ) );
        return enableCORS( Response.status(Status.NOT_FOUND) ).entity( niceMessage( message, "404 Resource Not Found: " + what ) ).build();
    }
    
	private static String brief( String message ) {
		int nl = message.indexOf( '\n' );
		return nl < 0 ? message : message.substring(0, nl) + "...";
	}

	private Response buildErrorResponse( EldaException e ) {
		return enableCORS( Response.status( e.code ) )
			.entity( niceMessage( e ) )
			.build()
			;
	}

	private static String niceMessage( String message ) {
		return niceMessage( message, "there seems to be a problem." );
	}
	
	private static String niceMessage( String message, String subText ) {
		return
			"<html>"
			+ "\n<head>"
			+ "\n<title>Error</title>"
			+ "\n</head>"
			+ "\n<body style='background-color: #ffeeee'>"
			+ "\n<h2>" + subText + "</h2>"
			+ "\n<p>" + message + "</p>"
			+ "\n</body>"
			+ "\n</html>"
			+ "\n"
			;
	}
    
   private String niceMessage( EldaException e ) {
		return
			"<html>"
			+ "\n<head>"
			+ "\n<title>Error " + e.code + "</title>"
			+ "\n</head>"
			+ "\n<body style='background-color: #ffdddd'>"
			+ "\n<h2>Error " + e.code + "</h2>"
			+ "\n<p>" + e.getMessage() + "</p>"
			+ (e.moreMessage == null ? "" : "<p>" + e.moreMessage + "</p>")
			+ "\n</body>"
			+ "\n</html>"
			+ "\n"
			;
	}
}

