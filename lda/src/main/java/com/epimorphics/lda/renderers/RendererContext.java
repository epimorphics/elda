/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers;

import java.util.Set;

import javax.servlet.ServletContext;

import com.epimorphics.lda.bindings.BindingSet;

public class RendererContext {

	protected final BindingSet v;
	protected final Spoo s;
	
	public RendererContext( BindingSet v, final ServletContext sc ) {
		this.v = v;
		this.s = new Spoo() {public String getWebRoot() { return sc.getContextPath(); }};
	}
	
	interface Spoo {
		String getWebRoot();
	}
	
	public RendererContext() {
		this.v = new BindingSet();
		this.s = new Spoo() { public String getWebRoot() { return ""; }};
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

	public String withWebRoot( String ePath ) {
		String wr = s.getWebRoot();
		return wr.equals( "" ) ? ePath : wr + "/" + ePath;
	}

}
