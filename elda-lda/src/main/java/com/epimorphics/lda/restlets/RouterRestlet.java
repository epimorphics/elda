/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.URLforResource;
import com.epimorphics.lda.cache.Cache;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.exceptions.*;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.renderers.velocity.VelocityRenderer;
import com.epimorphics.lda.routing.*;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.support.pageComposition.Messages;
import com.epimorphics.lda.support.statistics.StatsValues;
import com.epimorphics.util.*;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.WrappedException;
import com.sun.jersey.api.NotFoundException;

/**
 * Handles all incoming API calls and routes to appropriate locations.
 *
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
@Path("{path: .*}") public class RouterRestlet {

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String VARY = "Vary";
    public static final String ETAG = "Etag";
    public static final String LINK = "Link";
    public static final String EXPIRES = "Expires";
    public static final String LAST_MODIFIED_DATE = "Last-Modified-Date";

    final Router router;

	public static final String NO_EXPIRY = null;

	public static final Property ANY = null;

    /**
        TimestampedRouter is a router plus the timestamp of the latest file
        it was created from.
    */
    public static class TimestampedRouter {

    	static final long DEFAULT_INTERVAL = 5000;

    	// a year is forever.
		public static final long forever = 1000 * 60 * 24 * 365;
    
    	final Router router;
    	final long timestamp;
    	final long interval;
    
    	long nextCheck;
    
    	public TimestampedRouter(Router router, long when, long interval) {
    		this(router, when, interval, when + interval);
    	}
    
    	public TimestampedRouter(Router router, long when, long interval, long nextCheck) {
    		this.router = router;
    		this.timestamp = when;
    		this.interval = interval;
    		this.nextCheck = nextCheck;
    	}

		public void deferCheck() {
			nextCheck += interval;
		}
    }

    static final Map<String, TimestampedRouter> routers = new HashMap<String, TimestampedRouter>();

    /**
        Initialise this RouterRestlet. Happens a lot, so expensive
        initialisations should be cached. Sets the router used by
        this instance according to the appropriate LDA configs.
    */
    public RouterRestlet( @Context ServletContext con ) {
    	router = getRouterFor( con );
    }

    public static class Init implements ServletContextListener {

        static boolean announced = false;

	    @Override public void contextInitialized(ServletContextEvent sce) {
	    	ServletContext sc = sce.getServletContext();
			if (announced == false) {
				String baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
				
				String propertiesFile = System.getenv("ELDA_LOG4J_PROPERTIES");
				if (propertiesFile == null) propertiesFile = "log4j.properties";
				
				PropertyConfigurator.configure( baseFilePath + propertiesFile );
				log.info( "\n\n    =>=> Starting Elda (Init) {}\n", Version.string);
				announced = true;
			}
			getRouterFor( sc );
		}

		@Override public void contextDestroyed(ServletContextEvent sce) {
		}
	}

    public static int loadCounter = 0;
    
    /**
     	Answer a router initialised with the URI templates appropriate to
     	this context path. Such a router may already be in the routers table,
     	in which case it is used, otherwise a new router is created, initialised,
     	put in the table, and returned.
    */
     static synchronized Router getRouterFor(ServletContext con) {
    	 String contextPath = RouterRestletSupport.flatContextPath(con.getContextPath());
    	 TimestampedRouter r = routers.get(contextPath);
    	 long timeNow = System.currentTimeMillis();
    //
    	 if (r == null) {
    		 log.info("creating router for '{}'", contextPath);
    		 long interval = getRefreshInterval(contextPath);
    		 r = new TimestampedRouter( RouterRestletSupport.createRouterFor( con ), timeNow, interval );
    		 routers.put(contextPath, r );
    	 } else if (r.nextCheck < timeNow) {
	    	 long latestTime = RouterRestletSupport.latestConfigTime(con, contextPath);
	    	 if (latestTime > r.timestamp) {
	    		 log.info("reloading router for '{}'", contextPath);
	    		 long interval = getRefreshInterval(contextPath);
	    		 r = new TimestampedRouter( RouterRestletSupport.createRouterFor( con ), timeNow, interval );
	    		 loadCounter += 1;
	    		 DOMUtils.clearCache();
	    		 Cache.Registry.clearAll();
	    		 routers.put( contextPath, r );	    
	    	 } else {
	    		 // checked, but no change to reload
	    		 // log.debug("don't need to reload router, will check again later." );
	    		 r.deferCheck();
	    	 }
    	 } else {
    		 // Don't need to check yet, still in waiting period
    		 // log.debug("using existing router, not time to check yet.");
    	 }
    //
    	 return r.router;
     }

	private static long getRefreshInterval(String contextPath) {
		long delay = TimestampedRouter.DEFAULT_INTERVAL;
		String intervalFileName = "/etc/elda/conf.d/" + contextPath + "/delay.int";
		InputStream is = EldaFileManager.get().open( intervalFileName );
		 if (is != null) {
			String t = EldaFileManager.get().readWholeFileAsUTF8(is);
			try { is.close(); } catch (IOException e) { throw new WrappedException( e ); }
			long n = t.startsWith("FOREVER")
				? TimestampedRouter.forever
				: Long.parseLong(t.replace("\n", "" ))
				;
			if (n > 0) delay = n;
		 }
		 log.info("reload check interval for '{}' is '{}'", contextPath, delay);
		 return delay;
	}

	public Match getMatch( String path, MultiMap<String, String> queryParams ) {
        Match match = router.getMatch( path, queryParams );
        return match;
    }

    @GET @Produces
    	( {
    		"text/javascript"
    		, "application/javascript"
    		, "application/rdf+xml"
    		, "application/atom+xml"
    		, "application/json"
    		, "application/xml"
    		, "text/turtle"
    		, "text/html"
    		, "text/xml"
    		, "text/plain"
    	} )
    public Response requestHandler(
    		@Context HttpServletRequest servletRequest,
    		@Context HttpServletResponse servletResponse,
            @PathParam("path") String pathstub,
            @Context HttpHeaders headers,
            @Context ServletContext servCon,
            @Context UriInfo ui) throws IOException, URISyntaxException
    {    
    
//    	System.err.println(">> requestHandler: URI = " + ui.getRequestUri());
//    	System.err.println(">> path: " + pathstub);
    	
    	MultivaluedMap<String, String> rh = headers.getRequestHeaders();
    	String contextPath = servCon.getContextPath();
    
    	MultiMap<String, String> queryParams = JerseyUtils.convert(ui.getQueryParameters());
    	boolean dontCache = has( rh, "pragma", "no-cache" ) || has( rh, "cache-control", "no-cache" );
        Couple<String, String> pathAndType = parse( pathstub );
        Match matchAll = getMatch( "/" + pathstub, queryParams );
        Match matchTrimmed = getMatch( "/" + pathAndType.a, queryParams );
        Match match = matchTrimmed == null || notFormat( matchTrimmed, pathAndType.b ) ? matchAll : matchTrimmed;
    //
        String formatSuffix = match == matchAll ? null : pathAndType.b;
        Set<String> _formats = queryParams.getAll("_format");
        if (_formats.size() == 1) formatSuffix = _formats.iterator().next();
    //
        if (match == null) {
        	StatsValues.endpointNoMatch();
        	String item = router.findItemURIPath( "_", ui.getRequestUri(), "/" + pathAndType.a );
        	if (item == null) {
        		return noMatchFound( pathstub, ui, pathAndType );
        	}
        	else {
        		URI formatURI = DefaultRouter.accountForFormat(pathAndType.b, _formats, item);
				return standardHeaders( null, Response.seeOther( formatURI ) ).build();
        	}
        } else {
        //
        	String prefixPath = match.getEndpoint().getPrefixPath();
        	String path = contextPath + (prefixPath == null ? "" : prefixPath);
        	match.getEndpoint().getSpec().getBindings().put("_rootPath", path );
        //
        	Times t = new Times( pathstub );
        	Controls c = new Controls( !dontCache, t );
        	int encodingHash = hashOf( headers.getRequestHeaders().get("Accept-Encoding") );
        	int mediaHash = hashOf( headers.getAcceptableMediaTypes() );
			int runHash = mediaHash + encodingHash;
            List<MediaType> mediaTypes = JerseyUtils.getAcceptableMediaTypes( headers );
            Response answer = runEndpoint( c, contextPath, runHash, servCon, servletRequest, servletResponse, ui, queryParams, mediaTypes, formatSuffix, match );
            t.done();
            return answer;
        }
    }

	private int hashOf( Object x ) {
		return x == null ? 0x12345678 : x.hashCode();
	}

	private boolean has( MultivaluedMap<String, String> rh, String key, String value ) {
    	List<String> values = rh.get( key );
		return values != null && values.contains( value );
	}

	protected static final boolean showMightHaveMeant = false;

	private Response noMatchFound( String pathstub, UriInfo ui, Couple<String, String> pathAndType ) {
		String preamble = ui.getBaseUri().toString();
		String message = "Could not find anything matching " + ("/" + pathstub);
		if (pathAndType.b != null) message += " (perhaps '" + pathAndType.b + "' is an incorrect format name?)";
	//
		if (showMightHaveMeant) {
			message += "<div style='margin-bottom: 2px'>you might have meant any of:</div>\n";
			List<String> templates = router.templates();
			Collections.reverse( templates );
			for (String template: templates) {
				message += "\n<div style='margin-left: 2ex'>" + maybeLink(preamble, template) + "</div>";
			}
		}
		return returnNotFound( message + "\n", "/" + pathstub );
	}

    private String maybeLink( String preamble, String template ) {
    	if (template.contains( "{")) return template;
    	return "<a href='" + preamble + template.substring(1) + "'>" + template + "</a>";
    }

	/**
        Answer true of m's endpoint has no formatter called type.
    */
    private boolean notFormat( Match m, String type ) {
    	return m.getEndpoint().getRendererNamed( type ) == null;
	}

	//** return (revised path, renderer name or null)
    public static Couple<String, String> parse( String pathstub )
        {
        String path = pathstub, type = null;
        int dot = pathstub.lastIndexOf( '.' ) + 1;
        int slash = pathstub.lastIndexOf( '/' );
        if (dot > 0 && dot > slash)
            { path = pathstub.substring(0, dot - 1); type = pathstub.substring(dot); }
        return new Couple<String, String>( path, type );
        }

    static class Mutable<T> {
    
    	T value;
    
    	Mutable(T value) { this.value = value; }
    }

    // TODO : TOO MANY ARGUMENTS
    private Response runEndpoint
    	( final Controls c
    	, final String contextPath
    	, final int runHash
    	, final ServletContext servCon
    	, final HttpServletRequest servletRequest
    	, final HttpServletResponse servletResponse
    	, final UriInfo ui
    	, final MultiMap<String, String> queryParams
    	, final List<MediaType> mediaTypes
    	, final String initialFormatName
    	, final Match match
    	) {

    	String formatName = initialFormatName;
    	Bindings forErrorHandling = null;
    	URLforResource as = pathAsURLFactory(servCon);
    	URI requestUri = ui.getRequestUri();
    	log.debug("handling request '{}'", requestUri);
    //
        try {
        	URI ru = makeRequestURI(ui, match, requestUri);
        	APIEndpoint ep = match.getEndpoint();
        	boolean needsVaryAccept = formatName == null && queryParams.containsKey( "_format" ) == false;
        
        	Renderer _default = APIEndpointUtil.getRenderer( ep, formatName, mediaTypes );
        
        	if (formatName == null && _default != null) formatName = _default.getPreferredSuffix();
        
        	Renderer r = APIEndpointUtil.getRenderer( ep, formatName, mediaTypes );    
        	if (r == null) {
        		String message = formatName == null
        			? "no suitable media type was provided for rendering."
        			: "renderer '" + formatName + "' is not known to this server."
        			;
        		return standardHeaders( null, Response.status( Status.BAD_REQUEST )
        			.entity( Messages.niceMessage( message ) ) )
        			.build()
        			;
        	}
        //        
        	Bindings b = ep.getSpec().getBindings().copy();
        
        	String _properties = queryParams.getOne("_properties");
			b.put("_properties", _properties == null ? "" : _properties );

			String _page = queryParams.getOne("_page");
			b.put("_page", _page == null ? "" : _page );

			String _view = queryParams.getOne("_view");
			b.put("_view", _view == null ? "" : _view );

			b.putAny("_servletRequest", servletRequest);
			b.putAny("_servletResponse", servletResponse);

			forErrorHandling = new Bindings(b, as);

			List<String> formatNames = match.getEndpoint().getSpec().getRendererFactoryTable().formatNames();

        	APIEndpoint.Request req =
        		new APIEndpoint.Request( c, ru, b )
        		.withFormats( formatNames, formatName )
        		.withMode( r.getMode() )
        		;
        
        	ModelPrefixEditor mpe = ep.getSpec().getAPISpec().getModelPrefixEditor();
        //
        	NoteBoard nb = new NoteBoard();
        	nb.demandPage = r instanceof VelocityRenderer || r instanceof XMLRenderer;
        	        	
        	ResponseResult resultsAndBindings = APIEndpointUtil.call( req, nb, match, contextPath, queryParams );
        //
        //
        	if (resultsAndBindings.resultSet.isEmpty()) {
        		boolean isListEndpoint = ep.getSpec().isListEndpoint();
        		String exceptionIfEmpty = b.getAsString( "_exceptionIfEmpty", (isListEndpoint ? "no" : "yes" ));
				boolean return404IfEmpty = exceptionIfEmpty.equals( "yes" );
        		boolean throwToContainer = b.getAsString( "_passOnIfEmpty", "no" ).equals( "yes" );

        		if (return404IfEmpty) {
        			log.debug("resultSet is empty, returning status 404.");
        			if (throwToContainer) throw new NotFoundException();
        			return Response.status( Status.NOT_FOUND )
        					.type( "text/plain" )
        					.header( ACCESS_CONTROL_ALLOW_ORIGIN, "*" )
        					.header( VARY, "Accept" )
        					.entity( "404 Resource Not Found\n\n" + ru + "\n")
        					.build()
        					;
        		}
			}       
        //
        	Map<String, String> termBindings = mpe.rename( resultsAndBindings.uriToShortnameMap );
            APIResultSet results = resultsAndBindings.resultSet.applyEdits( mpe );
			Bindings rc = new Bindings( resultsAndBindings.bindings.copy(), as );
		//
        	if (_default.getPreferredSuffix().equals( r.getPreferredSuffix())) {
        		MediaType dmt = _default.getMediaType(rc);
        		if (!dmt.equals(r.getMediaType(rc))) {
        			r = RouterRestletSupport.changeMediaType( r, dmt );
        		}
        	}
        
        	long expiresAt = nb.expiresAt;
        
//        	System.err.println( ">> expiresAt: " + RouterRestletSupport.expiresAtAsRFC1123(expiresAt));
//        	System.err.println( ">> expiresAt: (= " + expiresAt + ")" );
//        	System.err.println( ">>  " + (expiresAt < System.currentTimeMillis() ? " expired" : " still alive" ) + ".");
        
        	String expiresDate = expiresAt < System.currentTimeMillis()
        		? NO_EXPIRY
        		: RouterRestletSupport.expiresAtAsRFC1123(expiresAt)
        		;

        	URI contentLocation = req.getURIwithFormat();
        
			MediaType mt = r.getMediaType(rc);
			log.debug("rendering with formatter '{}'", mt);
			Times times = c.times;
			Renderer.BytesOut bo = r.render( times, rc, termBindings, results );
			int mainHash = runHash + ru.toString().hashCode();
			return returnAs( contentLocation, expiresDate, results, mainHash + mt.hashCode(), wrap(times, bo), needsVaryAccept, mt );
	//
        } catch (StackOverflowError e) {
        	StatsValues.endpointException();
            log.error("Stack Overflow Error" );
            if (log.isDebugEnabled()) log.debug(Messages.shortStackTrace( e ));
            String message = Messages.niceMessage("Stack overflow", e.getMessage() );
            return ErrorPages.respond(forErrorHandling, servCon, "stack_overflow", message, EldaException.SERVER_ERROR);

        } catch (VelocityRenderingException e) {
            return ErrorPages.respond(forErrorHandling, servCon, "velocity_rendering", e.getMessage(), EldaException.SERVER_ERROR);

        } catch (BadRequestException e) {
        	return ErrorPages.respond(forErrorHandling, servCon, "bad_request", e.getMessage(), EldaException.BAD_REQUEST);
                    
        } catch (UnknownShortnameException e) {
        	log.error("UnknownShortnameException: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("{}", Messages.shortStackTrace( e ));
        	StatsValues.endpointException();
        	return ErrorPages.respond(forErrorHandling, servCon, "unknown_shortname", e.getMessage(), EldaException.BAD_REQUEST);

        } catch (EldaException e) {
        	StatsValues.endpointException();
        	log.error("Exception: {}", e.getMessage());
        	if (log.isDebugEnabled()) log.debug("{}", Messages.shortStackTrace( e ));
        	return ErrorPages.respond(forErrorHandling, servCon, "exception", e.getMessage(), EldaException.SERVER_ERROR);

        } catch (NotFoundException e) {
        	throw e;

        } catch (QueryParseException e) {
        	StatsValues.endpointException();
        	log.error("Query Parse Exception: {}", e.getMessage());
            if (log.isDebugEnabled()) log.debug("{}", Messages.shortStackTrace( e ));
            return ErrorPages.respond(forErrorHandling, servCon, "query_parse_exception", e.getMessage(), EldaException.SERVER_ERROR);

        } catch (Throwable e) {
        	log.error("general failure: {}", e.getClass().getCanonicalName() + ": " + e.getMessage());
        	e.printStackTrace(System.err);
        	StatsValues.endpointException();
        	return ErrorPages.respond(forErrorHandling, servCon, "general_exception", e.getMessage(), EldaException.SERVER_ERROR);
        }
    }

	private String getSeqID(HttpServletResponse servletResponse) {
		try { return servletResponse.getHeader(LogRequestFilter.X_RESPONSE_ID); }
		catch (NoSuchMethodError e) { return "none"; }
	}

    public static URI makeRequestURI(UriInfo ui, Match match, URI requestUri) {
		String base = match.getEndpoint().getSpec().getAPISpec().getBase();
		if (base == null) return requestUri;
		return URIUtils.resolveAgainstBase( requestUri, URIUtils.newURI( base ), ui.getPath() );
	}

    private static String MATCHES_SCHEME = "[a-zA-Z][-.+A-Za-z0-9]+:";

    private static String STARTS_WITH_SCHEME = "^" + MATCHES_SCHEME + ".*";

	private static URLforResource pathAsURLFactory(final ServletContext servCon) {
		return new URLforResource() {
			@Override public URL asResourceURL(String ePath) {
				try {
					if (ePath == null)
						throw new RuntimeException("problem: resource path in asResourceURL is null.");

					URL url = ePath.matches(STARTS_WITH_SCHEME) ? new URL(ePath)
						: ePath.startsWith("/") ? pathToURL(ePath)
						: pathToURL(getRealPath(servCon, "/" + ePath))
						;

					if (log.isDebugEnabled())
						log.debug("mapped ePath '{}' to '{}'", ePath, url);
					return url;
				} catch (MalformedURLException e) {
					throw new WrappedException(e);
				}
			}

			private String getRealPath(final ServletContext servCon, String ePath) {
				String realPath = servCon.getRealPath(ePath);
				if (realPath == null)
					throw new RuntimeException("problem: real path for " + ePath + " is null.");
				return realPath;
			}
		};
	}

	private static URL pathToURL(String path) throws MalformedURLException {
		return new File(path).toURI().toURL();
	}

    public static ResponseBuilder standardHeaders( String expiresDate, ResponseBuilder rb ) {
        return standardHeaders( expiresDate, null, 0, false, rb );
    }

    public static ResponseBuilder standardHeaders( String expiresDate, APIResultSet rs, int envHash, ResponseBuilder rb ) {
        return standardHeaders( expiresDate, rs, envHash, false, rb );
    }

    public static ResponseBuilder standardHeaders( String expiresDate, APIResultSet rs, int envHash, boolean needsVaryAccept, ResponseBuilder rb ) {
    	// rs may be null (no resultset for this header build)
    	rb = rb.header( ACCESS_CONTROL_ALLOW_ORIGIN, "*" );
        if (needsVaryAccept) rb = rb.header( VARY, "Accept" );
        if (expiresDate != null) rb = rb.header( EXPIRES, expiresDate );

//        Resource root = rs.getRoot();
//        List<Statement> licenses = rs.getMergedModel().listStatements(root, DCTerms.license, ANY).toList();
//        for(Statement l: licenses) {
//        	String ll = l.getObject().toString();
//        	rb = rb.header(LINK, "<" + ll + ">; rel=\"license\"");        
//        }

        if (rs != null && rs.enableETags()) rb = rb.tag( Long.toHexString( etagFor(rs, envHash) ) );
   		return rb;
    }

	private static long etagFor(APIResultSet rs, int envHash) {
		return rs.getHash() ^ envHash;
	}

    public static Response returnAs( String expiresDate, String response, String mimetype) {
        return standardHeaders( expiresDate, Response.ok(response, mimetype) ).build();
    }

    private static Response returnAs(URI contentLocation, String expiresDate, APIResultSet rs, int envHash, StreamingOutput response, boolean varyAccept, MediaType mt ) {
        try {
            return standardHeaders( expiresDate, rs, envHash, varyAccept, Response.ok( response, mt.toFullString() ) )
            	.contentLocation( contentLocation )
            	.build()
            	;
        } catch (RuntimeException e) {
            return returnError(e);
        }
    }

    private static StreamingOutput wrap( final Times t, final BytesOut response ) {
		return new StreamingOutput() {

			@Override public void write(OutputStream os) throws IOException, WebApplicationException {
				try {
					response.writeAll(t, os);
				}
				catch (Throwable e) {
					String message = String.format("Error while sending response: '%s'", e);
					log.error("{}", message);
					e.printStackTrace(System.err);
					StreamUtils.writeAsUTF8(response.getPoison() + "\n" + message, os);
					os.flush();
				}
				StatsValues.accumulate( t );
			}
		};
	}

	public static Response returnError(Throwable e ) {
        String shortMessage = e.getMessage();
		String longMessage = Messages.niceMessage( shortMessage, "Internal Server error." );
	//
		log.error("Exception: " + shortMessage );
        log.debug("{}", Messages.shortStackTrace( e ));
        return standardHeaders( null, Response.serverError() ).entity( longMessage ).build();
    }

	public static Response returnError(String s ) {
        log.error("exception: {}", s);
        return standardHeaders( null, Response.serverError() ).entity( s ).build();
    }

    public static Response returnNotFound( String message ) {
    	return returnNotFound( message, "" );
    }

    public static Response returnNotFound( String message, String what ) {
        log.debug("failed to return results: {}", Messages.brief( message ));
        if (true) throw new NotFoundException();
        String m = Messages.niceMessage( message, "404 Resource Not Found: " + what );
		return standardHeaders( null, Response.status(Status.NOT_FOUND) ).entity( m ).build();
    }

	private Response buildErrorResponse( EldaException e ) {
		return standardHeaders( null, Response.status( e.code ) )
			.entity( Messages.niceMessage( e ) )
			.build()
			;
	}
}

