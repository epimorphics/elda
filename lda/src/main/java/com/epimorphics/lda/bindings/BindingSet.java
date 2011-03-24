/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.hp.hpl.jena.util.OneToManyMap;

public class BindingSet implements Lookup, Iterable<Binding>
	{
	protected final Map<String, Binding> vars = new HashMap<String, Binding>();
	
	public BindingSet( BindingSet other ) 
		{ 
		this();
		putAll( other );
		}
	
	public BindingSet()
		{}
	
	public BindingSet putAll( BindingSet other ) 
		{
		for (Entry<String, Binding> e: other.vars.entrySet()) 
			this.vars.put( e.getKey(), e.getValue().copy() );
		return this;
		}
	
	@Override public Iterator<Binding> iterator() 
		{
		final Iterator<Map.Entry<String, Binding>> it = vars.entrySet().iterator();
		return new Iterator<Binding>() 
			{
			@Override public boolean hasNext() 
				{ return it.hasNext(); }
	
			@Override public Binding next() 
				{ return it.next().getValue(); }
	
			@Override public void remove() 
				{ it.remove(); }
			};
		}
	
	public Set<String> keySet() 
		{ return vars.keySet(); }
	
	public boolean hasVariable( String name ) 
		{ return vars.containsKey( name ); }
	
	public Binding get( String name ) 
		{ return vars.get( name ); }
	
	public String getAsString( String name ) 
		{ return vars.get( name ).valueString(); }
	
	public String getAsString( String name, String ifAbsent ) 
		{ return vars.containsKey( name ) ? vars.get( name ).valueString() : ifAbsent; }
	
	public BindingSet put( String name, String valueString )
		{ return put( name, new Binding( name, "", "", valueString ) ); }
		
	public BindingSet put( String name, Binding v ) 
		{ vars.put( name, v ); return this; }
	
	public void putInto( OneToManyMap<String, Binding> map ) 
		{ map.putAll( vars ); }
	
	public String toString()
		{ return "<variables " + vars.toString() + ">"; }
	
	public boolean equals( Object other )
		{ return other instanceof BindingSet && vars.equals( ((BindingSet) other).vars ); }
	
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
			sb.append( values.getAsString( s.substring( lb + 1, rb ) ) );
			start = rb + 1;
			}
		sb.append( s.substring( start ) );
		return sb.toString();
		}

	/**
	    Answer a new BindingSet constructed from the givem map
	    by converting the values into a plain string Binding object.
	*/
	public static BindingSet uplift( Map<String, String> bindings ) 
		{
		BindingSet result = new BindingSet();
		for (String key: bindings.keySet())
			result.put( key, new Binding( key, "", "", bindings.get( key ) ) );
		return result;
		}
	}