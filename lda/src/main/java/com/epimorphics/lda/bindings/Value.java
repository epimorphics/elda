/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;

/**
    A Value (of an Elda variable) has a type string (the URI of the type),
    a language code, and a value string. The type and language strings may
    be empty (note: not <code>null</code>, <i>empty</i>).
*/
public class Value
	{
	protected final String language;
	protected final String type;
	protected final String valueString;
	
	/**
	    A "zero value" for Value -- an untyped, unlanguaged,
	    empty string.
	*/
	public static final Value emptyPlain = new Value("");
	
	public Value( String valueString, String language, String type )
		{
		if (type == null) throw new IllegalArgumentException( "type must not be null (use \"\")" );
		this.type = type;
		this.language = language;
		this.valueString = valueString;
		}	
	
	public Value( String valueString ) 
		{ this( valueString, "", "" ); }
	
	public String valueString() 
		{ return valueString; }
	
	public Value withValueString( String vs ) 
		{ return new Value( vs, language, type ); }
	
	@Override public String toString()
		{ return "<value lang: " + language + " type: " + type + " value: " + valueString + ">"; }
	
	@Override public boolean equals( Object other ) 
		{ return other instanceof Value && same( (Value) other );	}
	
	private boolean same( Value other ) 
		{ return 
			this.language.equals( other.language )
			&& equals( this.type, other.type )
			&& equals( this.valueString, other.valueString )
			; 
		}
	
	private boolean equals( String a, String b ) 
		{ return a == null ? b == null : a.equals( b ); }
	
	@Override public int hashCode()
		{ return valueString.hashCode() + language.hashCode() + type.hashCode(); }
	}