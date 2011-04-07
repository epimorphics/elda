/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.OneToManyMap;

/**
    A VarValues maps variables (identified by their string names) to
    their Value (a lexical form with type & language annotation).
*/
public class VarValues implements Lookup
	{
    static Logger log = LoggerFactory.getLogger( VarValues.class );
    
	protected final Map<String, Value> vars = new HashMap<String, Value>();
	
	public VarValues( VarValues other ) 
		{ 
		this();
		putAll( other );
		}
	
	public VarValues()
		{}
	
	public VarValues putAll( VarValues other ) 
		{
		vars.putAll( other.vars );
		return this;
		}
	
	public Set<String> keySet() 
		{ return vars.keySet(); }
	
	public boolean hasVariable( String name ) 
		{ return vars.containsKey( name ); }
	
	public Value get( String name ) 
		{ return vars.get( name ); }
	
	public String getStringValue( String name ) 
		{ 
		Value v = vars.get( name );
		return v == null ? null : v.valueString(); 
		}
	
	public String getAsString( String name, String ifAbsent ) 
		{ return vars.containsKey( name ) ? vars.get( name ).valueString() : ifAbsent; }
	
	public VarValues put( String name, String valueString )
		{ return put( name, new Value( valueString ) ); }
		
	public VarValues put( String name, Value v ) 
		{ vars.put( name, v ); return this; }
	
	public void putInto( OneToManyMap<String, Value> map ) 
		{ map.putAll( vars ); }
	
	public String toString()
		{ return "<variables " + vars.toString() + ">"; }
	
	public boolean equals( Object other )
		{ return other instanceof VarValues && vars.equals( ((VarValues) other).vars ); }
	
	public int hashCode()
		{ return vars.hashCode(); }
	
	/**
	    Expands the string <code>s</code> by replacing any
	    occurrence of {wossname} by the value of wossname as
	    given by the Lookup <code>values</code>.
	*/
	public static String expandVariables( Lookup values, String s ) 
		{
		int start = 0;
		StringBuilder sb = new StringBuilder();
		while (true) 
			{
			int lb = s.indexOf( '{', start );
			if (lb < 0) break;
			int rb = s.indexOf( '}', lb );
			sb.append( s.substring( start, lb ) );
			String name = s.substring( lb + 1, rb );
			String value = values.getStringValue( name );
			if (value == null)
				log.warn( "variable " + name + " has no value, treated as empty string." );
			else
				sb.append( value );
			start = rb + 1;
			}
		sb.append( s.substring( start ) );
		return sb.toString();
		}

	/**
	    Answer a new BindingSet constructed from the given map
	    by converting the values into a plain string Binding object.
	*/
	public static VarValues uplift( Map<String, String> bindings ) 
		{
		VarValues result = new VarValues();
		for (String key: bindings.keySet())
			result.put( key, new Value( bindings.get( key ) ) );
		return result;
		}
	}