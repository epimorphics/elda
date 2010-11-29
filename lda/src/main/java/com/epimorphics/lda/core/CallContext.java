/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$

    File:        CallContext.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.OneToManyMap;

/**
 * Encapsulates all the information which define a particular
 * invocation of the API. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
*/
public class CallContext {

    static Logger log = LoggerFactory.getLogger( CallContext.class );
    
    protected OneToManyMap<String, String> parameters = new OneToManyMap<String, String>();
    protected UriInfo uriInfo;
    
    public CallContext(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
    
    /**
        Copy the given call-context, but add any bindings from the map
        for unset paramters.
    */
    public CallContext( Map<String, String> defaults, CallContext toCopy ) {
    	this.uriInfo = toCopy.uriInfo;
    	this.parameters.putAll( defaults );
    	this.parameters.putAll( toCopy.parameters );
    }
    
	public static CallContext createContext( UriInfo ui, Map<String, String> bindings ) {
	    MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
	    CallContext cc = new CallContext(ui);
	    for (Map.Entry<String, String> e : bindings.entrySet()) {
	        cc.parameters.put( e.getKey(), e.getValue() );
	    }
	    for (Map.Entry<String, List<String>> e : queryParams.entrySet()) {
	        String param = e.getKey();
	        for (String val : e.getValue())
				cc.parameters.put(param, val);
	    }
	    return cc;
	}

    /**
     * Return a single value for a parameter, if there are multiple values
     * the returned one may be arbitrary
     */
    public String getParameterValue(String param) {
        String v = parameters.get(param);
		return v == null ? uriInfo.getQueryParameters().getFirst( param ) : v;
    }
    
    /**
     * Return the request URI information.
     */
    public UriInfo getUriInfo() {
        return uriInfo;
    }
    
    public String toString() {
        return parameters.toString();
    }
    
    /**
     * Return a UriBuilder initialized from the query, to allow
     * modified versions of query to be generated
     */
    public UriBuilder getURIBuilder() {
       return uriInfo.getRequestUriBuilder(); 
    }
    
    /**
        Answer the set of filter names from the call context query parameters.
    */
    public Set<String> getFilterPropertyNames() {
    	return new HashSet<String>( uriInfo.getQueryParameters().keySet() );    	
    }

    /**
        Expand <code>valueString</code> by replacing "{name}" by the value
        of that name. Answer the updated string.
    */
	public String expand( String valueString ) {
		StringBuilder result = new StringBuilder();
		int anchor = 0;
		while (true) {
			int lbrace = valueString.indexOf( '{', anchor );
			if (lbrace < 0) break;
			int rbrace = valueString.indexOf( '}', lbrace );
			result.append( valueString.substring( anchor, lbrace ) );
			String innerName = valueString.substring( lbrace + 1, rbrace );
			String evaluated = this.getParameterValue( innerName );
			if (evaluated == null) {
				log.warn( "variable " + innerName + " has no value, treated as empty string." );
			} else {
				result.append( evaluated );
			}
			anchor = rbrace + 1;
		}
		result.append( valueString.substring( anchor ) );
		return result.toString();			
	}
}

