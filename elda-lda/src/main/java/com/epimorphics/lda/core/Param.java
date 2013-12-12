/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.core;

import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.lda.bindings.Bindings;
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
	    	
	static class PrefixedParam extends Param 
		{
		final String prefix;
		
		protected PrefixedParam( ShortnameService sns, String prefix, String p ) 
			{ super( sns, p ); this.prefix = prefix; }
		
		@Override public String toString()
			{ return prefix + "--" + p; }

		@Override public Param expand(Bindings cc) 
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

		@Override public Param expand(Bindings cc) 
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
		public final String typeURI;
		
		private Info(Resource r, String p, String typeURI) 
			{
			this.asResource = r;
			this.shortName = p;
			this.typeURI = typeURI;
			this.asURI = RDFQ.uri( r.getURI() );
			}

		public static Info create( ShortnameService sns, String p ) 
			{
			Resource r = sns.asResource(p);
			ContextPropertyInfo prop = sns.asContext().getPropertyByName( p );
			String type = prop == null ? null : prop.getType();
			return new Info(r, p, type);
			}
		
		@Override public String toString() 
			{
			return "<Info " + shortName + " is " + asResource + ", with type " + typeURI + ">";
			}
		}
	
	public Info[] fullParts() 
		{
		String [] parts = p.split("\\.");
		Info [] result = new Info[parts.length];
		for (int i = 0; i < result.length; i += 1) result[i] = Info.create(sns, parts[i]);
		return result;
		}
	
	public abstract Param expand( Bindings cc );

	public abstract String prefix();

	public abstract Param plain();
	}