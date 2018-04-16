package com.epimorphics.lda.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class MetaConfig {
	
	protected boolean disableDefaultMetadata = false;
	
	public Set<Property> enabled = new HashSet<Property>();
	
	public Map<String, Resource> blocks = new HashMap<String, Resource>();
	
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
		loadMetadataBlocks(root);
	}

	private void loadMetadataBlocks(Resource root) {
		List <Statement> mds = root.listProperties(ELDA_API.metadata).toList();		
		for (Statement md: mds) loadMetadataBlock(md);
	}

	private void loadMetadataBlock(Statement md) {
				
		Resource block = md.getResource();
		String name = RDFUtils.getStringValue(block, API.name);
				
		List<Statement> ss = block.listProperties().toList();
				
		Model target = ModelFactory.createDefaultModel();
		
		for (Statement s: ss) {
			if (s.getPredicate().equals(API.name)) {
				// discard
			} else {
				target.add(s);
			}
		}
		
		Resource outBlock = block.inModel(target);
		blocks.put(name,  outBlock);
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
		return "<mc " + disableDefaultMetadata + " " + enabled + ">" + blocks;
	}

	public void addMetadata(Resource root) {		
		Model target = root.getModel();
		
		for (String k: blocks.keySet()) {	
			Resource block = blocks.get(k);
		
			for (Statement s: block.listProperties().toList()) {
				target.add(root, s.getPredicate(), s.getObject());
			}
		}
	}
}
