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
import com.epimorphics.util.DOMUtils.Mode;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    The built-in table of renderers, by name. Includes those defined
    by the LDA spec.
    
 	@author chris
*/
public class BuiltinRendererTable {
	
	private static class XSLT_thingy implements RendererFactory {
		
		private final Resource root;
		private final String mediaType;
		
		XSLT_thingy( Resource root, String mediaType ) {
			this.root = root;		
			this.mediaType = mediaType;
		}
		
		@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
			String sheet = root.getProperty( API.stylesheet ).getString();
			return new XMLRenderer( sns, Mode.TRANSFORM, mediaType, sheet );
		}

		@Override public RendererFactory withResource( Resource r ) {
			return new XSLT_thingy( r, mediaType );
		}

		@Override public RendererFactory withMediaType( String mediaType ) {
			return new XSLT_thingy( root, mediaType );
		}
	}

	private abstract static class DoingWith implements RendererFactory {
		
		@Override public RendererFactory withResource( Resource r ) {
			return this;
		}
		
		@Override public RendererFactory withMediaType( String type ) {
			return this;
		}
	}
	
	static private Factories factoryTable = new Factories();
	
	static private Map<Resource, RendererFactory> builtins = new HashMap<Resource, RendererFactory>();
	
	static void putFactory( String name, Resource type, String mime, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mime, rf );
		builtins.put( type, rf );
	}
	
	static void putDefaultFactory( String name, Resource type, String mime, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mime, rf, true );
		builtins.put( type, rf );
	}
	
	public static RendererFactory getFactory( Resource type ) {
		return builtins.get( type );
	}
	
	static {
		putFactory( "text", API.RdfXmlFormatter, "text/plain", new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep, "text/plain");
			}
			} );
		
		putFactory( "ttl", API.TurtleFormatter, "text/turtle", new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new TurtleRenderer();
			}
			} );
		
		 putFactory( "rdf", API.RdfXmlFormatter, "application/rdf+xml", new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new RDFXMLRenderer();
			}
			} );
		
		putDefaultFactory( "json", API.JsonFormatter, "application/json", new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep );
			}
			} );
		
		putFactory( "xml", API.XmlFormatter, "application/xml", new DoingWith() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, Mode.AS_IS );
			}
			} );
		
		putFactory( "_xslt", API.XsltFormatter, "*/*", new XSLT_thingy( null, "*/*" ) );
		
		putFactory( "html", FIXUP.HtmlFormatter, "text/html", new DoingWith() 
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
