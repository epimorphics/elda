/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.util.Map;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Production of XSLT renderers, which transform the results
    of an XML rendering.
*/
public class XSLT_RendererFactory implements RendererFactory {
	
	private final CompleteContext.Mode defaultMode = Mode.PreferLocalnames;

	private final Resource root;
	private final MediaType mt;
	private final CompleteContext.Mode mode;
	
	XSLT_RendererFactory( Resource root, MediaType mt ) {
		this.root = root;		
		this.mt = mt;
		this.mode = Mode.decode(root, defaultMode);
	}
	
	@Override public Renderer buildWith( final APIEndpoint ep, final ShortnameService sns ) {
		return new Renderer() {

			@Override public MediaType getMediaType( Bindings irrelevant ) {
				return mt;
			}
		    
		    @Override public Mode getMode() {
		    	return mode;
		    }

		    @Override public String getPreferredSuffix() {
		    	return "html"; 
		    }

			@Override public Renderer.BytesOut render( Times t, Bindings rc, Map<String, String> termBindings, APIResultSet results ) {
				handleMetadata(results);
				final String sheet = rc.expandVariables(root.getProperty( API.stylesheet ).getString());
				final XMLRenderer xr = new XMLRenderer( mode, sns, mt, sheet );
				return xr.render( t, rc.copyWithDefaults( ep.defaults() ), termBindings, results ); 
			}

			public void handleMetadata( APIResultSet results ) {
				String [] options = MetadataOptions.get( root );
				if (options.length == 0) options = "bindings,formats,versions,execution".split(",");
				results.includeMetadata( options );
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