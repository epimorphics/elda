/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.core;

import com.epimorphics.jsonrdf.Context.Prop;
import com.epimorphics.lda.shortnames.ShortnameService;

/**
 	introduced to try and pull apart the types of arguments to the different filtering
 	functions so that they're not all strings.
*/
public abstract class Param
	{
	final String p;
	
	protected Param( String p ) { this.p = p; }
	
	public static Param make( ShortnameService sns, String p ) 
		{ 
		if (p.charAt(0) == '_') return new MagicParam( p );
		int hyphen = p.indexOf('-');
		if (hyphen < 0)
			{
			return new PlainParam( p );
			}
		else
			{
			String prefix = p.substring(0, hyphen);
			String name = p.substring(hyphen+1);
			return new PrefixedParam( prefix, p );
			}
		}

	protected static void munge( ShortnameService sns, String p ) 
		{
		String [] parts = p.split("\\.");
		for (String part: parts)
			{
			if (part.charAt(0) == '{')
				{ /* deferred */ }
			else
				{
				Prop prop = sns.asContext().getPropertyByName( part );
				if (prop == null) throw new RuntimeException( "property '" + part + "' isn't defined." );
				else System.err.println( "]]  type: " + prop.getType() );
				}
			}
		}
	    	
	static class MagicParam extends Param 
		{
		protected MagicParam( String p ) 
			{ super( p ); }

		@Override public Param substring(int n) 
			{ throw new RuntimeException( "cannot substring magic parameter " + p );}

		@Override public Param expand(CallContext cc) 
			{ throw new RuntimeException( "cannot expand magic parameter " + p );}
		}
	    	
	static class PrefixedParam extends Param 
		{
		final String prefix;
		
		protected PrefixedParam( String prefix, String p ) 
			{ super( p ); this.prefix = prefix; }

		@Override public Param substring(int n) 
			{ return new PrefixedParam(prefix, p.substring(n)); }

		@Override public Param expand(CallContext cc) 
			{ return new PrefixedParam( prefix, cc.expandVariables( p ) ); }
		}
	
	static class PlainParam extends Param
		{
		protected PlainParam( String p )
			{ super( p ); }

		@Override public Param substring(int n) 
			{ return new PlainParam(p.substring(n)); }

		@Override public Param expand(CallContext cc) 
			{ return new PlainParam( cc.expandVariables( p ) ); }
		}

	public String lastPropertyOf() {
		String [] parts = this.asString().split( "\\." );
		return parts[parts.length - 1];
	}
	
	@Override public String toString() { return p; }
	
	public String asString() { return p; }
	
	public boolean is( String thing ) { return p.equals(thing); }
	
	public boolean hasPrefix(String s) { return p.startsWith(s); }
	
	public abstract Param substring(int n);
	
	public String[] parts() { return p.split("\\."); }
	
	public boolean hasVariable() { return p.indexOf('{') >= 0; }
	
	public abstract Param expand( CallContext cc ); 
	}