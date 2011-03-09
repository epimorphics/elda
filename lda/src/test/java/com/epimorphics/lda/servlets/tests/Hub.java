package com.epimorphics.lda.servlets.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.renderers.JSONRenderer;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.restlets.RouterRestlet;
import com.epimorphics.lda.routing.Match;
import com.epimorphics.lda.support.MultiValuedMapSupport;
import com.epimorphics.lda.tests.APITesterUriInfo;
import com.epimorphics.util.Couple;

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
		+ ", " + JSONRenderer.JSON_MIME
		+ ", " + XMLRenderer.XML_MIME
		+ ", text/html"
		;
	
	public void doGet( HttpServletRequest req, HttpServletResponse res) 
		throws IOException, ServletException 
		{
		List<MT> types = mediaType( req.getHeaders( "accept" ));
		String acceptedType = accept( types, acceptable );
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
    
	private void GO( HttpServletRequest req, HttpServletResponse res, String acceptedType ) throws IOException
		{
		String pathstub = req.getPathInfo();
		UriInfo ui = new APITesterUriInfo( "SPOO/FLARN", MultiValuedMapSupport.parseQueryString( "" ) );
	//
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
            CallContext cc = CallContext.createContext( ui, match.getBindings(), acceptedType );
            APIEndpoint ep = match.getEndpoint();
            log.debug("Info: calling APIEndpoint " + ep.getSpec());
            try 
            	{
                Couple<APIResultSet, String> resultsAndFormat = ep.call( cc );
				APIResultSet results = resultsAndFormat.a;
                if (results == null) 
                	{
                    res.sendError( SC_NOT_FOUND, "No answer back from " + ep.getSpec() );
                	} 
                else 
                	{ // TODO fix the switch from media types to named renderers.
                	Renderer r = ep.getRendererNamed( acceptedType );
                	System.err.println( "r = " + r + " for " + acceptedType );
                	Renderer.Params rp = RouterRestlet.paramsFromContext( cc );
                	String result = r.render( rp, results );
//                	String cl = results.getContentLocation();
                	res.setContentType( acceptedType );
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
	
	private String accept( List<MT> types, String canHandle ) 
		{
		List<MT> served = decodeTypes( canHandle );
		for (MT t: types)
			for (MT s: served)
				if (t.accepts( s )) return s.A + "/" + s.B;
		return null;
		}

	static class MT
		{
		final String A;
		final String B;
		final float Q;
		
		MT( String A, String B, float Q )
			{ this.A = A; this.B = B; this.Q = Q; }
		
		public boolean accepts( MT s ) 
			{
			return (A.equals("*") || A.equals( s.A )) && (B.equals("*") || B.equals( s.B ));
			}

		@Override public String toString()
			{ return A + "/" + B + "; q=" + Q; }
		}

	private static final Comparator<? super MT> compareMT = new Comparator<MT>() 
		{
		@Override public int compare( MT a, MT b ) 
			{
			if (a.Q > b.Q) return -1;
			if (a.Q > b.Q) return +1;
			if (a.A.equals( "*" )) return -1;
			if (b.A.equals( "*" )) return +1;
			if (a.B.equals( "*" )) return -1;
			if (b.B.equals( "*" )) return +1;
			return 0;
			}
		};
	
	private List<MT> mediaType( Enumeration e ) 
		{
		List<MT> types = new ArrayList<MT>();
		while (e.hasMoreElements()) types.addAll( decodeTypes( (String) e.nextElement() ) );
		Collections.sort( types, compareMT );
		return types;
		}

	private List<MT> decodeTypes( String a ) 
		{
		List<MT> result = new ArrayList<MT>();
		if (a.length() > 0)
			for (String one: a.split( " *, *" ))
				result.add( decodeType( one ) );
		return result;
		}

	private MT decodeType( String one )
		{
		float Q = 1.0f;
		String[] X = one.split( " *; *" );
		for (int i = 1; i < X.length; i += 1)
			if (X[i].startsWith("q="))
				Q = Float.parseFloat( X[1].substring(2));
		String [] AB = X[0].split( "/" );
		return new MT( AB[0], AB[1], Q );
		}

	private String names( Enumeration e ) 
		{
		StringBuilder b = new StringBuilder();
		while (e.hasMoreElements()) b.append( " | " ).append( e.nextElement() );
		return b.toString();
		}
	}
