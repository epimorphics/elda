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

/**
    A VarValues maps variables (identified by their string names) to
    their Value (a lexical form with type & language annotation).
*/
public class VarValues implements Lookup
	{
    static Logger log = LoggerFactory.getLogger( VarValues.class );
    
    protected static final Set<String> emptySet = new HashSet<String>();
    
	protected final Map<String, Value> vars = new HashMap<String, Value>();
    
    protected final Set<String> parameterNames = new HashSet<String>();
	
    public VarValues( Set<String> parameterNames, VarValues initial ) 
    	{
    	this.putAll( initial );
    	this.parameterNames.addAll( parameterNames );
    	}

	/**
	    Initialise this VarValues by copying the entries from
	    <code>other</code>.
	*/
	public VarValues( VarValues initial ) 
		{ this.putAll( initial ); }
	
	public VarValues( Set<String> parameterNames ) 
		{ this.parameterNames.addAll( parameterNames ); }
	
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
	    Answer the set of parameter names (a subset of the
	    variable names).
	*/
	public Set<String> parameterNames() 
		{ return new HashSet<String>( parameterNames ); }
		
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
		{ 
		Value v = vars.get( name );
		return v == null || v.isComplete() ? v : evaluate( name, v, new ArrayList<String>() ); 
		}  
	
	/**
	    Answer the lexical form of the value of the
	    variable <code>name</code> in this VarValues, or
	    null if it is not bound. Part of the implementation
	    of <code>Lookup</code>.
	*/
	@Override public String getValueString( String name ) 
		{ 
		Value v = get( name );
		return v == null ? null : v.valueString(); 
		}
	
	/**
	    Answer the lexical form of the value of the variable <code>name</code>,
	    or the value of <code>ifAbsent</code> if it is not bound.
	*/
	public String getAsString( String name, String ifAbsent ) 
		{ return vars.containsKey( name ) ? get( name ).valueString() : ifAbsent; }
	
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
	    Answer a String which displays the content of this
	    VarValues.
	*/
	@Override public String toString()
		{ return "<variables " + vars.toString() + ">"; }
	
	/**
	    Answer true if <code>other</code> is an instance of VarValues,
	    their maps have the same keys, and the post-evaluation value of
	    the variables is the same.
	*/
	@Override public boolean equals( Object other )
		{ return other instanceof VarValues && same( (VarValues) other ); }

	/**
	    The long way round, because it will force evaluation of {...} variables.
	*/
	private boolean same( VarValues other ) 
		{
		Set<String> keys = vars.keySet();
		if (!keys.equals( other.vars.keySet() )) 	return false;
		for (String key: keys)
			if (!get(key).equals( other.get(key) )) return false;
		return true;
		}
	
	/**
	    Answer a suitable hashcode for this VarValues.
	*/
	@Override public int hashCode()
		{ return vars.hashCode(); }
	
	private Value evaluate( String name, Value v, List<String> seen ) 
		{
		String expanded = expandVariables( v.valueString, seen );
		if (v.valueString.equals( expanded )) return v;
		Value newV = v.withValueString( expanded );
		vars.put( name, newV );
		return newV;
		}	
	
	public String expandVariables( String s, List<String> seen ) 
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
						
			if (seen.contains( name )) 
				throw new RuntimeException( "circularity involving: " + seen );
			
			Value thisV = vars.get(name);

			// Patch to allow missing variables (which blow up evaluate, so
			// we're bypassing for now).
			if (thisV == null) 
				{
				sb.append( "{" ).append( name ).append( "}" );
				// log.warn( "variable " + name + " has no value, not substituted." );
				}
			else
				{
				seen.add( name );
				Value v = evaluate( name, thisV, seen );
				seen.remove( seen.size() - 1 );
				String value = v.valueString; // values.getStringValue( name );
				if (value == null)
					{
					sb.append( "{" ).append( name ).append( "}" );
					log.warn( "variable " + name + " has no value, not substituted." );
					}
				else
					sb.append( value );
				}
			start = rb + 1;
			}
		sb.append( s.substring( start ) );
		return sb.toString();
		}
	
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
			String value = values.getValueString( name );
			if (value == null)
				{
				sb.append( "{" ).append( name ).append( "}" );
				log.warn( "variable " + name + " has no value, not substituted." );
				}
			else
				sb.append( value );
			start = rb + 1;
			}
		sb.append( s.substring( start ) );
		return sb.toString();
		}

	/**
	    Answer a new VarValues constructed from the given map
	    by converting the values into a string-valued Value.
	*/
	public static VarValues uplift( Map<String, String> bindings ) 
		{
		VarValues result = new VarValues();
		for (String key: bindings.keySet())
			result.put( key, new Value( bindings.get( key ) ) );
		return result;
		}
	}