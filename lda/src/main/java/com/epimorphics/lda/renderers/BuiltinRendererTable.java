/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.util.MediaType;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The built-in table of renderers, by name. Includes those defined
    by the LDA spec.
    
 	@author chris
*/
public class BuiltinRendererTable {
	
	private abstract static class DoingWith implements RendererFactory {
		
		@Override public RendererFactory withRoot( Resource r ) {
			return this;
		}
		
		@Override public RendererFactory withMediaType( MediaType mt ) {
			return this;
		}
	}
	
	static private Factories factoryTable = new Factories();
	
	static private Map<Resource, RendererFactory> builtins = new HashMap<Resource, RendererFactory>();
	
	static void putFactory( String name, Resource type, MediaType mt, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mt, rf );
		builtins.put( type, rf );
	}
	
	static void putDefaultFactory( String name, Resource type, MediaType mt, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mt, rf, true );
		builtins.put( type, rf );
	}
	
	public static RendererFactory getFactory( Resource type ) {
		return builtins.get( type );
	}
	
	static {
		putFactory( "text", API.RdfXmlFormatter, MediaType.TEXT_PLAIN, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep, MediaType.TEXT_PLAIN );
			}
			} );
		
		putFactory( "ttl", API.TurtleFormatter, MediaType.TEXT_TURTLE, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new TurtleRenderer();
			}
			} );
		
		 putFactory( "rdf", API.RdfXmlFormatter, MediaType.APPLICATION_RDF_XML, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new RDFXMLRenderer();
			}
			} );
		
		putDefaultFactory( "json", API.JsonFormatter, MediaType.APPLICATION_JSON, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep );
			}
			} );
		
		putFactory( "xml", API.XmlFormatter, MediaType.APPLICATION_RDF_XML, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns );
			}
			} );
		
		putFactory( "_xslt", API.XsltFormatter, MediaType.STAR_STAR, new XSLT_RendererFactory( null, MediaType.STAR_STAR ) );
		
		putFactory( "html", FIXUP.HtmlFormatter, MediaType.TEXT_HTML, new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new HTMLRenderer();
			}
			} );
	}
	
	public static Factories getBuiltinRenderers() {
		return factoryTable.copy();
	}
	
	public static boolean isRendererType( Resource r ) {
		return builtins.get( r ) != null;
	}

}
