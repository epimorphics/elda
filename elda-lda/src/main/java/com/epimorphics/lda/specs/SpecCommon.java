package com.epimorphics.lda.specs;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;

public class SpecCommon {

	final Resource root;
	
	final Set<RDFNode> licences = new HashSet<RDFNode>();

	public SpecCommon(Set<RDFNode> inheritedLicences, Resource root) {
		this(root);
		licences.addAll(inheritedLicences);
	}
	
	public SpecCommon(Resource root) {
		this.root = root;
		for (RDFNode x: root.listProperties(ELDA_API.license).mapWith(Statement.Util.getObject).toList()) {
			licences.add(x);
		}
	}
	
	public Set<RDFNode> getLicenceNodes() {
		return new HashSet<RDFNode>(licences);
	}
	
	public Set<Resource> getNotices() {
		Set<Resource> result = new HashSet<Resource>();
		for (RDFNode x: root.listProperties(ELDA_API.notice).mapWith(Statement.Util.getObject).toList()) {
			result.add(x.asResource());
		}
		return result;		
	}
}
