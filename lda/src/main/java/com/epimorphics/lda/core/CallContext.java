/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        CallContext.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.bindings.Value;
import com.epimorphics.lda.bindings.VarValues;
import com.hp.hpl.jena.util.OneToManyMap;

/**
 * Encapsulates all the information which define a particular
 * invocation of the API. 
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
public class CallContext implements Lookup {

    static Logger log = LoggerFactory.getLogger( CallContext.class );
    
    protected OneToManyMap<String, Value> parameters = new OneToManyMap<String, Value>();
    protected UriInfo uriInfo = null;
    
    public CallContext(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }
    
    /**
        Copy the given call-context, but add any bindings from the map
        for unset parameters.
    */
    public CallContext( VarValues defaults, CallContext toCopy ) {
    	this.uriInfo = toCopy.uriInfo;
    	defaults.putInto( this.parameters );
    	this.parameters.putAll( toCopy.parameters );
    }
    
	public static CallContext createContext( UriInfo ui, VarValues bindings ) {
	    MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
	    CallContext cc = new CallContext( ui );
	    bindings.putInto( cc.parameters );
	    for (Map.Entry<String, List<String>> e : queryParams.entrySet()) {
	        String name = e.getKey();
			Value basis = cc.parameters.get( name );
			if (basis == null) basis = Value.emptyPlain;
	        for (String val : e.getValue())
				cc.parameters.put( name, basis.withValueString( val ) );
	    }
	    return cc;
	}
	
	public Iterator<String> parameterNames() {
		return parameters.keySet().iterator();
	}

    /**
     * Return a single value for a parameter, if there are multiple values
     * the returned one may be arbitrary
     */
    @Override public String getStringValue( String param ) {
        Value v = parameters.get( param );
		return v == null ? uriInfo.getQueryParameters().getFirst( param ) : v.valueString();
    }
    
    /**
     * Return the request URI information.
     */
    public UriInfo getUriInfo() {
        return uriInfo;
    }
    
    @Override public String toString() {
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
	public String expandVariables( String valueString ) {
		return VarValues.expandVariables( this, valueString );			
	}
}

