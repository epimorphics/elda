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

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.bindings.Lookup;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.shared.WrappedException;

/**
	The context in which a rendering takes place. It provides access to the value
	of bound variables and a way of transforming a file path into a URL (which
	can then be used to access the contents of the specified file).
*/
public class RendererContext implements Lookup {

	protected final VarValues v;
	protected final AsURL s;
	protected final String contextPath;
	
	public RendererContext( VarValues v, String contextPath, AsURL as, final ServletContext sc ) {
		this.v = v;
		this.s = new AsURL() 
			{@Override public URL asResourceURL( String p ) 
				{ try {
					URL result = sc.getResource( p );
					if (result == null) EldaException.NotFound( "webapp resource", p );
					return result;
				} catch (MalformedURLException e) {
					throw new WrappedException( e );
				} }
			};
		this.contextPath = contextPath;
	}
	
	public RendererContext( VarValues v ) {
		this.v = v;
		this.contextPath = "";
		this.s = new AsURL() 
			{@Override public URL asResourceURL( String p ) { throw new RuntimeException( "this context can't make a URL for " + p ); }};
	}
	
	public interface AsURL {
		URL asResourceURL( String u );
	}
	
	public RendererContext() {
		this( new VarValues() );
	}
	
	public String getAsString( String key, String ifAbsent ) {
		return v.getAsString( key, ifAbsent );
	}

	@Override public String getStringValue( String key ) {
		return v.getStringValue( key );
	}

	public Set<String> keySet() {
		return v.keySet();
	}

	public void put( String key, String value ) {
		v.put( key, value );		
	}
	
	public String getContextPath() {
		return contextPath;
	}

	public URL pathAsURL( String ePath ) {
		String p = ePath.startsWith( "/" ) || ePath.startsWith( "http://") ? ePath : "/" + ePath;
		return s.asResourceURL( p );
	}

	@Override public Set<String> getStringValues( String name ) {
		return CollectionUtils.set( getStringValue( name ) );
	}

}
