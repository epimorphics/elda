/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.MultiMap;
import com.epimorphics.util.CollectionUtils;

import static java.util.Collections.unmodifiableSet;

/**
    A VarValues maps variables (identified by their string names) to
    their Value (a lexical form with type & language annotation).
*/
public class VarValues implements Lookup
	{
    static Logger log = LoggerFactory.getLogger( VarValues.class );
    
	protected final Map<String, Value> vars = new HashMap<String, Value>();
	
	/**
	    Initialise this VarValues by copying the entries from
	    <code>other</code>.
	*/
	public VarValues( VarValues other ) 
		{ 
		this();
		putAll( other );
		}
	
	/**
	    Initialise this ValueValues to have no bindings.
	*/
	public VarValues()
		{}
	
	/**
	    Add all the entries from <code>other</code> to this 
	    ValValues, overwriting any existing bindings with 
	    the same names. Answer this VarValues.
	*/
	public VarValues putAll( VarValues other ) 
		{
		vars.putAll( other.vars );
		return this;
		}
	
	/**
	    Answer a set of the variable names bound in this
	    ValValues.
	*/
	public Set<String> keySet() 
		{ return vars.keySet(); }
	
	/**
	    Answer true iff there is a binding for the variable
	    called <code>name</code> in this VarValues.
	*/
	public boolean hasVariable( String name ) 
		{ return vars.containsKey( name ); }
	
	/**
	    Answer the Value of the variable <code>name</code> in
	    this VarValues, or null if it is not bound.
	*/
	public Value get( String name ) 
		{ return vars.get( name ); }
	
	/**
	    Answer the lexical form of the value of the
	    variable <code>name</code> in this VarValues, or
	    null if it is not bound. Part of the implementation
	    of <code>Lookup</code>.
	*/
	@Override public String getStringValue( String name ) 
		{ 
		Value v = vars.get( name );
		return v == null ? null : v.valueString(); 
		}
	
	/**
	    An immutable Set of no strings.
	*/ 
	static final Set<String> NoStrings = unmodifiableSet( new HashSet<String>() );
	
	/**
	    Answer an immutable set of the lexical form of the variable
	    <code>name</code> or an empty set if it is unbound. 
	*/
	@Override public Set<String> getStringValues( String name ) 
		{ 
		Value v = vars.get( name );
		return v == null ? NoStrings : unmodifiableSet( CollectionUtils.set( v.valueString() ) ); 
		}
	
	/**
	    Answer the lexical form of the value of the variable <code>name</code>,
	    or the value of <code>ifAbsent</code> if it is not bound.
	*/
	public String getAsString( String name, String ifAbsent ) 
		{ return vars.containsKey( name ) ? vars.get( name ).valueString() : ifAbsent; }
	
	/**
	    Bind <code>name</code> to a Value which is a plain string
	    with the given <code>valueString</code> as its lexical form.
	    Any existing binding for <code>name</code> is discarded.
	    Answer this VarValues.
	*/
	public VarValues put( String name, String valueString )
		{ return put( name, new Value( valueString ) ); }
		
	/**
	    Bind <code>name</code> to the value <code>v</code>.
	    Discard any existing binding for <code>name</code>.
	    Answer this VarValues.
	*/
	public VarValues put( String name, Value v ) 
		{ vars.put( name, v ); return this; }
	
	/**
	    Add to the MultiMap <code>map</code> all of the
	    bindings in this VarValues.
	*/
	public void putInto( MultiMap<String, Value> map ) 
		{ map.putAll( vars ); }
	
	/**
	    Answer a String which displays the content of this
	    VarValues.
	*/
	@Override public String toString()
		{ return "<variables " + vars.toString() + ">"; }
	
	/**
	    Answer true if <code>other</code> is an instance of VarValues
	    and they wrap .equals() maps.
	*/
	@Override public boolean equals( Object other )
		{ return other instanceof VarValues && vars.equals( ((VarValues) other).vars ); }
	
	/**
	    Answer a suitable hashcode for this VarValues.
	*/
	@Override public int hashCode()
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