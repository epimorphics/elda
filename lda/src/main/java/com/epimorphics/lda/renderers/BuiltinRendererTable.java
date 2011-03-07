/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.routing.Loader;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.DOMUtils.As;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The built-in table of renderers, by name. Includes those defined
    by the LDA spec.
    
 	@author chris
*/
public class BuiltinRendererTable {
	
	private static String transformFilepath() {
		String bfp = Loader.getBaseFilePath();
		System.err.println( ">> bfp = " + bfp );
		return bfp + "xsltsheets/results.xsl";
	}
	
	static private Factories factoryTable = new Factories();
	
	static {
		factoryTable.putFactory( "text", API.RdfXmlFormatter, "text/plain", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep, "text/plain");
			}
			} );
		
		factoryTable.putFactory( "ttl", API.TurtleFormatter, "text/turtle", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new TurtleRenderer();
			}
			} );
		
		factoryTable.putFactory( "rdf", API.RdfXmlFormatter, "application/rdf+xml", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new RDFXMLRenderer();
			}
			} );
		
		factoryTable.putFactory( "json", API.JsonFormatter, "application/json", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep );
			}
			}, true );
		
		factoryTable.putFactory( "xml", API.XmlFormatter, "application/xml", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, As.XML );
			}
			} );
		
		factoryTable.putFactory( "html", FIXUP.HtmlFormatter, "text/html", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, As.HTML, transformFilepath() );
			}
			} );
	}
	
	public static Factories getBuiltinRenderers() {
		return factoryTable.copy();
	}

	public static RendererFactory factoryWithURI( Resource r ) {
		return factoryTable.getFactoryByURI( r );
	}

}
