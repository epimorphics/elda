/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.servlet.ServletContext;

import com.epimorphics.lda.bindings.BindingSet;

public class RendererContext {

	protected final BindingSet v;
	protected final Spoo s;
	
	public RendererContext( BindingSet v, final ServletContext sc ) {
		this.v = v;
		this.s = new Spoo() 
			{public URL asResourceURL( String p ) 
				{ try {
					return sc.getResource( p );
				} catch (MalformedURLException e) {
					throw new RuntimeException( e );
				} }
			};
	}
	
	public RendererContext( BindingSet v ) {
		this.v = v;
		this.s = new Spoo() 
			{public URL asResourceURL( String p ) { throw new RuntimeException( "this context can't make a URL for " + p ); }};
	}
	
	interface Spoo {
		URL asResourceURL( String u );
	}
	
	public RendererContext() {
		this.v = new BindingSet();
		this.s = new Spoo() 
			{public URL asResourceURL( String p ) { throw new RuntimeException( "this context can't make a URL for " + p ); }};
	}
	
	public String getAsString( String key, String ifAbsent ) {
		return v.getAsString( key, ifAbsent );
	}

	public String getAsString( String key ) {
		return v.getAsString( key );
	}

	public Set<String> keySet() {
		return v.keySet();
	}

	public void put(String key, String value ) {
		v.put( key, value );		
	}

	public URL toURL( String ePath ) {
		String p = ePath.startsWith( "/" ) ? ePath : "/" + ePath;
		return s.asResourceURL( p );
	}

}
