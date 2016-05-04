package com.epimorphics.lda.specs;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class SpecCommon {

	final Set<RDFNode> licenceProperties = new HashSet<RDFNode>();
	
	public SpecCommon(Resource root) {
		
	}
	
	public Set<RDFNode> getLicenceNodes() {
		return new HashSet<RDFNode>(licenceProperties);
	}
}
