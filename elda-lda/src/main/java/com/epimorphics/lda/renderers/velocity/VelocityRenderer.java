package com.epimorphics.lda.renderers.velocity;

import java.io.OutputStream;
import java.util.Map;

import org.apache.velocity.app.VelocityEngine;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.BytesOutTimed;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.Resource;

public class VelocityRenderer implements Renderer {
	
	final MediaType mt;
	final String suffix;
	final Resource config;
	private VelocityCore core;
	final String templateName;
	final String [] metadataOptions;

	static final String[] defaultMetadataOptions = "bindings,formats,versions,execution".split(",");
	
	public VelocityRenderer( MediaType mt, Bindings b, Resource config ) {
		this.mt = mt;
		this.suffix = RDFUtils.getStringValue( config, API.name, "html" );
		this.templateName = RDFUtils.getStringValue( config, EXTRAS.velocityTemplate, "page-shell.vm" );
		this.metadataOptions = getMetadataOptions( config );
		this.config = config;
	}

	public String[] getMetadataOptions(Resource config) {
		String [] options = MetadataOptions.get( config );
		return options.length == 0 ? defaultMetadataOptions : options;
	}

	@Override public MediaType getMediaType( Bindings irrelevant ) {
        return mt;
    }

    @Override public String getPreferredSuffix() {
    	return suffix;
    }
    
    @Override public Mode getMode() {
    	return Mode.PreferLocalnames;
    }
    
    @Override public Renderer.BytesOut render
    	( Times t
    	, final Bindings b
    	, Map<String, String> termBindings
    	, final APIResultSet results 
    	) {
    // Issue whether we need to reconstruct thing every time round
    // We used to use the bindings that came in when the renderer
    // was constructed.
    	VelocityEngine ve = Help.createVelocityEngine( b, config );
		this.core = new VelocityCore( ve, suffix, templateName );
	//
    	return new BytesOutTimed() {

			@Override public void writeAll( OutputStream os ) {
				results.includeMetadata( metadataOptions );
				core.render( results, b, os );
			}			

			@Override protected String getFormat() {
				return "html";
			}
    		
    	};
    }
}