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

import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.bindings.Value;
import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.exceptions.EldaException;

/**
 * Encapsulates all the information which define a particular
 * invocation of the API. 
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
public class CallContext implements Lookup {
    
    final protected VarValues values = new VarValues();
    
    final protected MultiMap<String, String> queryParameters;
    
    final protected Set<String> parameterNames;
        
    private CallContext( MultiMap<String, String> queryParameters ) {
        this.queryParameters = queryParameters;
        this.parameterNames = new HashSet<String>( queryParameters.keySet() );
    }
    
    /**
        Take a copy of this context. All the VarValues in the
        defaults are added unless they are already bound.
    */
    public CallContext copyWithDefaults( VarValues defaults ) {
    	CallContext result = new CallContext( this.queryParameters );
    	result.values.putAll( defaults ); 
    	result.values.putAll( this.values );
        return result;
    }

	public static CallContext createContext( VarValues bindings, MultiMap<String, String> queryParams ) {
	    CallContext cc = new CallContext( queryParams );
	    cc.values.putAll( bindings ); 
	    for (String name: queryParams.keySet()) {
	    	Set<String> values = queryParams.getAll( name );
	    	if (values.size() > 1) EldaException.BadRequest("Multiple values for parameter '" + name + "': feature not implemented.");
			Value basis = cc.values.get( name );
			if (basis == null) basis = Value.emptyPlain;
			cc.values.put( name, basis.withValueString( values.iterator().next() ) );
	    }
	    return cc;
	}
	
	public Iterator<String> parameterNames() {
		return values.keySet().iterator();
	}

	public Value getParameter( String name ) {
		return values.get( name );
	}
	
    /**
     * Return a single value for a parameter, if there are multiple values
     * the returned one may be arbitrary
     */
    @Override public String getStringValue( String param ) {
        Value v = values.get( param );
		return v == null ? queryParameters.getOne( param ) : v.valueString();
    }
    
    @Override public String toString() {
        return values.toString();
    }
    
    /**
        Answer the set of filter names from the call context query parameters.
    */
    public Set<String> getFilterPropertyNames() {
    	return new HashSet<String>( parameterNames );    	
    }

    /**
        Expand <code>valueString</code> by replacing "{name}" by the value
        of that name. Answer the updated string.
    */
	public String expandVariables( String valueString ) {
		return VarValues.expandVariables( this, valueString );			
	}
}

