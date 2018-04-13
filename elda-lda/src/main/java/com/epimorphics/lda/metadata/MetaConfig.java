package com.epimorphics.lda.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class MetaConfig {
	
	protected boolean disableDefaultMetadata = false;
	
	protected Set<Property> enabled = new HashSet<Property>();
	
	public MetaConfig(boolean disableDefaultMetadata) {
		this.disableDefaultMetadata = disableDefaultMetadata;
	}
	
	public MetaConfig() {
		this(false);
	}
	
	public MetaConfig(Resource root, MetaConfig metaConfig) {
		this(metaConfig.disableDefaultMetadata);
	}
	
	public MetaConfig(Resource root) {
		List<Statement> ss = root.listProperties(ELDA_API.enable_default_metadata).toList();
		for (Statement s: ss) {
			List< RDFNode> l = s.getObject().as(RDFList.class).asJavaList();
			for (RDFNode n: l) {
				Property p = n.as(Property.class);
				enabled.add(p);
			}
		}
	}

	public boolean disableDefaultMetadata() {
		return disableDefaultMetadata;
	}
	
	public boolean drop(Property p) {
		return !keep(p);
	}
	
	protected boolean keep(Property p) {
		return 
			enabled.contains(p)
			|| disableDefaultMetadata == false
			;
	}
}
