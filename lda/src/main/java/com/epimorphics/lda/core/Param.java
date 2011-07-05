/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.core;

import com.epimorphics.jsonrdf.Context.Prop;
import com.epimorphics.lda.rdfq.RDFQ;
import com.epimorphics.lda.rdfq.URINode;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 	introduced to try and pull apart the types of arguments to the different filtering
 	functions so that they're not all strings.
*/
public abstract class Param
	{
	final String p;
	final ShortnameService sns;
	
	protected Param( ShortnameService sns, String p ) { this.p = p; this.sns = sns; }
	
	public static Param make( ShortnameService sns, String p ) 
		{ 
		if (p.charAt(0) == '_') return new ReservedParam( p );
		int hyphen = p.indexOf('-');
		if (hyphen < 0)
			{
			return new PlainParam( sns, p );
			}
		else
			{
			String prefix = p.substring(0, hyphen+1);
			String name = p.substring(hyphen+1);
			return new PrefixedParam( sns, prefix, name );
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
	    	
	static class ReservedParam extends Param 
		{
		protected ReservedParam( String p ) 
			{ super( null, p ); }

		@Override public Param expand(CallContext cc) 
			{ throw new RuntimeException( "cannot expand reserved parameter " + p );}

		@Override public String prefix()
			{ return null; }

		@Override public Param plain() 
			 { throw new RuntimeException( "cannot make plain a reserved parameter: " + p ); }
		}
	    	
	static class PrefixedParam extends Param 
		{
		final String prefix;
		
		protected PrefixedParam( ShortnameService sns, String prefix, String p ) 
			{ super( sns, p ); this.prefix = prefix; }
		
		@Override public String toString()
			{ return prefix + "--" + p; }

		@Override public Param expand(CallContext cc) 
			{ return new PrefixedParam( sns, prefix, cc.expandVariables( p ) ); }

		@Override public String prefix() 
			{ return prefix; }

		@Override public Param plain() 
			{ return new PlainParam( sns, p ); }
		}
	
	static class PlainParam extends Param
		{
		protected PlainParam( ShortnameService sns, String p )
			{ super( sns, p ); }

		@Override public Param expand(CallContext cc) 
			{ return new PlainParam( sns, cc.expandVariables( p ) ); }

		@Override public String prefix()
			{ return null; }

		@Override public Param plain() 
			 { throw new RuntimeException( "cannot make plain a plain parameter: " + p ); }
		}

	public String lastPropertyOf() {
		String [] parts = this.asString().split( "\\." );
		return parts[parts.length - 1];
	}
	
	@Override public String toString() { return p; }
	
	public String asString() { return p; }
	
	public static class Info
		{
		public final String shortName;
		public final Resource asResource;
		public final URINode asURI;
		
		public Info(Resource r, String p) 
			{
			this.asResource = r;
			this.shortName = p;
			this.asURI = RDFQ.uri( r.getURI() );
			}

		public static Info create( ShortnameService sns, String p ) 
			{
			return new Info(sns.normalizeResource(p), p);
			}
		}
	
	public Info[] fullParts() 
		{
		String [] parts = p.split("\\.");
		Info [] result = new Info[parts.length];
		for (int i = 0; i < result.length; i += 1) result[i] = Info.create(sns, parts[i]);
		return result;
		}
	
	public boolean hasVariable() { return p.indexOf('{') >= 0; }
	
	public abstract Param expand( CallContext cc );

	public abstract String prefix();

	public abstract Param plain();
	}