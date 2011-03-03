/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.routing.Loader;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.DOMUtils.As;

/**
    The built-in table of renderers, by name. Includes those defined
    by the LDA spec.
    
 	@author chris
*/
public class BuiltinRendererTable {
	
	static private Map<String, RendererFactory> factoryTable = new HashMap<String, RendererFactory>();


	private static String transformFilepath() {
		String bfp = Loader.getBaseFilePath();
		System.err.println( ">> bfp = " + bfp );
		return bfp + "xsltsheets/results.xsl";
	}
	
	static {
		factoryTable.put( "text", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep, "text/plain");
			}
			} );
		
		factoryTable.put( "ttl", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new TurtleRenderer();
			}
			} );
		
		factoryTable.put( "rdf", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new RDFXMLRenderer();
			}
			} );
		
		factoryTable.put( "json", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep );
			}
			} );
		
		factoryTable.put( "xml", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, As.XML );
			}
			} );
		
		factoryTable.put( "html", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, As.HTML, transformFilepath() );
			}
			} );
	}
	
	public static Map<String, RendererFactory>getBuiltinRenderers() {
		return new HashMap<String, RendererFactory>( factoryTable );
	}

}
