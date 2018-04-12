package com.epimorphics.lda.metadata;

import com.hp.hpl.jena.rdf.model.Resource;

public class MetaConfig {
	
	protected boolean disableDefaultMetadata = false;
	
	public MetaConfig(boolean disableDefaultMetadata) {
		this.disableDefaultMetadata = disableDefaultMetadata;
	}
	
	public MetaConfig() {
		this(false);
	}
	
	public MetaConfig(Resource root, MetaConfig metaConfig) {
		this(metaConfig.disableDefaultMetadata);
	}

	public boolean disableDefaultMetadata() {
		return disableDefaultMetadata;
	}
}