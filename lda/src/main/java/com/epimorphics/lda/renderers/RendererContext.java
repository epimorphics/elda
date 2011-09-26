/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers;

import java.net.URL;
import java.util.Set;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.bindings.Lookup;

/**
	The context in which a rendering takes place. It provides access to the value
	of bound variables and a way of transforming a file path into a URL (which
	can then be used to access the contents of the specified file).
*/
public class RendererContext implements Lookup {

	protected final VarValues v;
	protected final AsURL as;
	protected final String contextPath;
	
	/**
	    Initialise this RendererContext with a bunch of variable bindings
	    <code>v</code>, a string to use as the <code>contextPath</code>, and
	    an AsURL object which converts (partial) paths to full URLs.    
	*/
	public RendererContext( VarValues v, String contextPath, AsURL as ) {
		this.v = v;
		this.as = as;
		this.contextPath = contextPath;
	}
	
	public RendererContext( VarValues v ) {
		this.v = v;
		this.contextPath = "";
		this.as = new AsURL() 
			{@Override public URL asResourceURL( String p ) { throw new RuntimeException( "this context can't make a URL for " + p ); }};
	}
	
	/**
	    A interface of one method which maps a possibly partial path
	    to a URL.
	    
	 	@author chris
	*/
	public interface AsURL {
		URL asResourceURL( String u );
	}
	
	public RendererContext() {
		this( new VarValues() );
	}
	
	public String getAsString( String key, String ifAbsent ) {
		return v.getAsString( key, ifAbsent );
	}

	@Override public String getValueString( String key ) {
		return v.getValueString( key );
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
		return as.asResourceURL( p );
	}
}
