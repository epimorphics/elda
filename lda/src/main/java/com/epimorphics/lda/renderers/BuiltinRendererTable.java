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
		
		XSLT_thingy() {
			this( null );
		}
		
		XSLT_thingy( Resource root ) {
			this.root = root;
		}
		
		@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
			String sheet = root.getProperty( API.stylesheet ).getString();
			return new XMLRenderer( sns, Mode.TRANSFORM, sheet );
		}

		@Override public RendererFactory withResource( Resource r ) {
			return new XSLT_thingy( r );
		}
	}

	private static String transformFilepath() {
		String bfp = Loader.getBaseFilePath();
		System.err.println( ">> bfp = " + bfp );
		return bfp + "xsltsheets/results.xsl";
	}
	
	static private Factories factoryTable = new Factories();
	
	static private Map<Resource, RendererFactory> builtins = new HashMap<Resource, RendererFactory>();
	
	
	static void putFactory( String name, Resource type, String mime, RendererFactory rf ) {
		factoryTable.putFactory( name, null, mime, rf );
		builtins.put( type, rf );
	}
	
	public static RendererFactory getFactory( Resource type ) {
		return builtins.get( type );
	}
	
	static {
		putFactory( "text", API.RdfXmlFormatter, "text/plain", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep, "text/plain");
			}
			
			@Override public RendererFactory withResource( Resource r ) {
				return this;
			}
			} );
		
		putFactory( "ttl", API.TurtleFormatter, "text/turtle", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new TurtleRenderer();
			}
			
			@Override public RendererFactory withResource( Resource r ) {
				return this;
			}
			} );
		
		 putFactory( "rdf", API.RdfXmlFormatter, "application/rdf+xml", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new RDFXMLRenderer();
			}
			
			@Override public RendererFactory withResource( Resource r ) {
				return this;
			}
			} );
		
		putFactory( "json", API.JsonFormatter, "application/json", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new JSONRenderer( ep );
			}
			
			@Override public RendererFactory withResource( Resource r ) {
				return this;
			}
			} ); // TODO , true );
		
		putFactory( "xml", API.XmlFormatter, "application/xml", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, Mode.AS_IS );
			}
			
			@Override public RendererFactory withResource( Resource r ) {
				return this;
			}
			} );
		
		putFactory( "_xslt", API.XsltFormatter, "*/*", new XSLT_thingy() );
		
		putFactory( "html", FIXUP.HtmlFormatter, "text/html", new RendererFactory() 
			{
			@Override public Renderer buildWith( APIEndpoint ep, ShortnameService sns ) {
				return new XMLRenderer( sns, Mode.TRANSFORM, transformFilepath() );
			}
			
			@Override public RendererFactory withResource( Resource r ) {
				return this;
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
