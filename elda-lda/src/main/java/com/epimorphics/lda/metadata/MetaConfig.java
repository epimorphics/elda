package com.epimorphics.lda.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class MetaConfig {
	
	protected boolean disableDefaultMetadata = false;
	
	public Set<Property> enabled = new HashSet<Property>();
	
	public MetaConfig(boolean disableDefaultMetadata) {
		this.disableDefaultMetadata = disableDefaultMetadata;
	}
	
	public MetaConfig() {
		this(false);
	}
	
	public MetaConfig(Resource root, MetaConfig mc) {
		this.disableDefaultMetadata = mc.disableDefaultMetadata;
		this.enabled = new HashSet<Property>(mc.enabled);
		parseRoot(root);
	}
	
	public MetaConfig(Resource root) {
		parseRoot(root);
	}

	private void parseRoot(Resource root) {
		enableDefaultMetadata(root);
		disableDefaultMetadata(root);
		loadBlockMetadata(root);
	}

	private void loadBlockMetadata(Resource root) {
		
	}

	private void disableDefaultMetadata(Resource root) {
		List<Statement> dd = root.listProperties(ELDA_API.disable_default_metadata).toList();
		for (Statement d: dd) {
			boolean disable = d.getBoolean();
			disableDefaultMetadata = disable;
		}
	}

	private void enableDefaultMetadata(Resource root) {
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
	
	@Override public String toString() {
		return "<mc " + disableDefaultMetadata + " " + enabled + ">";
	}

	public void addMetadata(Model receiver) {
		
	}
}
