/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.RDFUtils;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Production of XSLT renderers, which transform the results
    of an XML rendering.
*/
public class XSLT_RendererFactory implements RendererFactory {
	
	private final Resource root;
	private final MediaType mt;
	
	XSLT_RendererFactory( Resource root, MediaType mt ) {
		this.root = root;		
		this.mt = mt;
	}
	
	@Override public Renderer buildWith( final APIEndpoint ep, final ShortnameService sns ) {
		return new Renderer() {

			@Override public MediaType getMediaType( RendererContext irrelevant ) {
				return mt;
			}

			@Override public String render( RendererContext rc, APIResultSet results ) {
				final String meta = RDFUtils.getStringValue( root, EXTRAS.metadataOptions, null );
				if (meta != null) results.includeMetadata( meta.split( "," ) );
				final String sheet = root.getProperty( API.stylesheet ).getString();
				final XMLRenderer xr = new XMLRenderer( sns, mt, sheet );
				return xr.render( rc, results ); 
			}
		}; 
	}

	@Override public RendererFactory withRoot( Resource r ) {
		return new XSLT_RendererFactory( r, mt );
	}

	@Override public RendererFactory withMediaType( MediaType mt ) {
		return new XSLT_RendererFactory( root, mt );
	}
}