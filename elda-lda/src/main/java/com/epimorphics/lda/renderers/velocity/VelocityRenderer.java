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
	final VelocityCore core;
	final String suffix;
	final String [] metadataOptions;

	static final String[] defaultMetadataOptions = "bindings,formats,versions,execution".split(",");
	
	public VelocityRenderer( MediaType mt, Bindings b, Resource config ) {
		VelocityEngine ve = Help.createVelocityEngine( b, config );
		String templateName = RDFUtils.getStringValue( config, EXTRAS.velocityTemplate, "page-shell.vm" );
		this.suffix = RDFUtils.getStringValue( config, API.name, "html" );
		this.mt = mt;
		this.core = new VelocityCore( ve, suffix, templateName );
		this.metadataOptions = getMetadataOptions( config );
	}

	public String[] getMetadataOptions(Resource config) {
		String [] options = MetadataOptions.get( config );
		return options.length == 0 ? defaultMetadataOptions : options;
	}

	@Override public MediaType getMediaType( Bindings irrelevant ) {
        return mt;
    }

    @Override public String getPreferredSuffix() {
    	return "suffix";
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