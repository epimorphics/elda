/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.bindings;

public class Binding
	{
	protected final String name;
	protected final String language;
	protected final String type;
	protected String valueString;
	
	public Binding( String name, String language, String type, String valueString )
		{
		this.name = name;
		this.type = type;
		this.language = language;
		this.valueString = valueString;
		}	
	
	public static Binding make( String name, Binding already ) 
		{ return already == null ? new Binding( name, "", "", null ) : already; }	
	
	public Binding copy() 
		{ return new Binding( name, language, type, valueString ); }
	
	public String valueString() 
		{ return valueString; }
	
	public String name() 
		{ return name; }
	
	public Binding withValueString( String vs ) 
		{ return new Binding( name, language, type, vs ); }
	
	public String toString()
		{ return "<var name=" + name + " lang: " + language + " type: " + type + " value: " + valueString + ">"; }
	
	public boolean equals( Object other ) 
		{ return other instanceof Binding && same( (Binding) other );	}
	
	private boolean same( Binding other ) 
		{ return 
			this.name.equals( other.name )
			&& this.language.equals( other.language )
			&& equals( this.type, other.type )
			&& equals( this.valueString, other.valueString )
			; 
		}
	
	private boolean equals( String a, String b ) 
		{ return a == null ? b == null : a.equals( b ); }
	
	public int hashCode()
		{ return name.hashCode() + language.hashCode() + type.hashCode(); }
	}