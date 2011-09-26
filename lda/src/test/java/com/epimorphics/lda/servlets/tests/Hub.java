package com.epimorphics.lda.servlets.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.RendererContext;
import com.epimorphics.lda.restlets.RouterRestlet;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.tests_support.MakeData;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.Triad;
import com.epimorphics.util.Util;

import static javax.servlet.http.HttpServletResponse.*;

/**
    NOT FOR PUBLIC USE -- it's a Jerseyless version under development.
    
 	@author chris
*/
public class Hub extends HttpServlet
	{
	private static final long serialVersionUID = 719130733256615295L;
	
	static final String acceptable = 
		"text/plain"
		+ ", text/turtle"
		+ ", application/rdf+xml"
		+ ", " + MediaType.APPLICATION_JSON.toString()
		+ ", " + MediaType.APPLICATION_RDF_XML.toString()
		+ ", text/html"
		;
	
	@Override public void doGet( HttpServletRequest req, HttpServletResponse res) 
		throws IOException, ServletException 
		{
		@SuppressWarnings("unchecked") List<MediaType> types = MediaType.mediaType( req.getHeaders( "accept" ));
		MediaType acceptedType = MediaType.accept( types, acceptable );
		if (acceptedType == null)
			{
			res.sendError( SC_NOT_ACCEPTABLE, "none of " + types.toString() + " are acceptable." );
			return;
			}
		else
			{
			 GO( req, res, acceptedType );
			}
		}

    static Logger log = LoggerFactory.getLogger( Hub.class );
    
	private void GO( HttpServletRequest req, HttpServletResponse res, MediaType acceptedType ) throws IOException
		{
		String pathstub = req.getPathInfo();
		// CORS, see http://www.w3.org/wiki/CORS_Enabled
		res.setHeader( RouterRestlet.ACCESS_CONTROL_ALLOW_ORIGIN, "*" );
	//
        Match match = RouterRestlet.getMatch( pathstub );
        if (match == null) 
        	{
            res.sendError( SC_NOT_FOUND, "no API handler for " + pathstub );
        	} 
        else 
        	{
        	MultiMap<String, String> map = MakeData.parseQueryString( "" );
        	URI ru = Util.newURI( "SPOO/FLARN" );
            CallContext cc = CallContext.createContext( map, match.getBindings() );
            APIEndpoint ep = match.getEndpoint();
            log.debug("Info: calling APIEndpoint " + ep.getSpec());
            try 
            	{
                Triad<APIResultSet, String, CallContext> resultsAndFormat = ep.call( ru, cc );
				APIResultSet results = resultsAndFormat.a;
                if (results == null) 
                	{
                    res.sendError( SC_NOT_FOUND, "No answer back from " + ep.getSpec() );
                	} 
                else 
                	{ // TODO fix the switch from media types to named renderers.
                	Renderer r = ep.getRendererByType( acceptedType );
                	System.err.println( "r = " + r + " for " + acceptedType );
                	RendererContext rp = new RendererContext( RouterRestlet.paramsFromContext( cc ) );
                	String result = r.render( rp, results );
//                	String cl = results.getContentLocation();
                	res.setContentType( acceptedType.toString() );
            		PrintWriter out = res.getWriter();
            		out.print( result );
                	out.close();
                	}
            	} 
            catch (Throwable t) 
            	{
            	res.sendError( SC_INTERNAL_SERVER_ERROR, t.toString() );
            	t.printStackTrace( System.err );
            	}
        	}
    
		}
	}
