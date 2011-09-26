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

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.bindings.Value;
import com.epimorphics.lda.bindings.VarValues;

/**
 * Encapsulates all the information which define a particular
 * invocation of the API. 
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
public class CallContext implements Lookup {

    static Logger log = LoggerFactory.getLogger( CallContext.class );
    
    final protected VarValues parameters = new VarValues();
    
    final protected MultiMap<String, String> queryParameters;
        
    private CallContext( MultiMap<String, String> queryParameters ) {
        this.queryParameters = queryParameters;
    }
    
    /**
        Copy the given call-context, but add any bindings from the map
        for unset parameters.
    */
    public CallContext( VarValues defaults, CallContext toCopy ) {
    	this.parameters.putAll( defaults ); // defaults.putInto( this.parameters );
    	this.parameters.putAll( toCopy.parameters );
        this.queryParameters = toCopy.queryParameters;
    }

	public static CallContext createContext( MultiMap<String, String> queryParams, VarValues bindings ) {
	    CallContext cc = new CallContext( queryParams );
	    cc.parameters.putAll( bindings ); // bindings.putInto( cc.parameters );
	    for (String name: queryParams.keySet()) {
			Value basis = cc.parameters.get( name );
			if (basis == null) basis = Value.emptyPlain;
	        for (String val : queryParams.getAll( name ))
				cc.parameters.put( name, basis.withValueString( val ) );
	    }
	    return cc;
	}
	
	public Iterator<String> parameterNames() {
		return parameters.keySet().iterator();
	}

	public Value getParameter( String name ) {
		return parameters.get( name );
	}
	
    /**
     * Return a single value for a parameter, if there are multiple values
     * the returned one may be arbitrary
     */
    @Override public String getStringValue( String param ) {
        Value v = parameters.get( param );
		return v == null ? queryParameters.getOne( param ) : v.valueString();
    }
    
    /**
     	Return all the values for a parameter.
    */
    @Override public Set<String> getStringValues( String param ) {
        Value v = parameters.get( param );
		Set<String> values = queryParameters.getAll( param );
		return v == null ? values : asStrings( v );
    }
    
    private Set<String> asStrings( Value v) {
    	Set<String> result = new HashSet<String>();
    	result.add( v.valueString() );
    	return result;
	}
    
    @Override public String toString() {
        return parameters.toString();
    }
    
    /**
        Answer the set of filter names from the call context query parameters.
    */
    public Set<String> getFilterPropertyNames() {
    	return new HashSet<String>( queryParameters.keySet() );    	
    }

    /**
        Expand <code>valueString</code> by replacing "{name}" by the value
        of that name. Answer the updated string.
    */
	public String expandVariables( String valueString ) {
		return VarValues.expandVariables( this, valueString );			
	}
}

