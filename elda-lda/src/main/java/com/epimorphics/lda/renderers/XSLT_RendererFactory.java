/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.BrokenException;

/**
    Production of XSLT renderers, which transform the results
    of an XML rendering.
*/
public class XSLT_RendererFactory implements RendererFactory {
	
	private final CompleteContext.Mode defaultMode = Mode.PreferLocalnames;

	private final Resource root;
	private final MediaType mt;
	private final CompleteContext.Mode mode;
	
	public XSLT_RendererFactory( Resource root, MediaType mt ) {
		
		if (root == null) throw new BrokenException(">> BUMPSY-WUMPSY.");
		
		this.root = root;		
		this.mt = mt;
		this.mode = Mode.decode(root, defaultMode);
	}
	
	@Override public Renderer buildWith( final APIEndpoint ep, final ShortnameService sns ) {
		return new Renderer() {

			@Override public MediaType getMediaType( Bindings unused ) {
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
				final String sheet = rc.expandVariables( objectSpelling( root.getProperty( API.stylesheet ) ));
				final XMLRenderer xr = new XMLRenderer( mode, sns, mt, sheet );
				return xr.render( t, rc.copyWithDefaults( ep.defaults() ), termBindings, results ); 
			}

			private String objectSpelling(Statement s) {
				Node ob = s.getObject().asNode();
				return ob.isLiteral() ? ob.getLiteralLexicalForm() : ob.getURI();
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