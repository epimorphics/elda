package com.epimorphics.lda.specs;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;

public class SpecCommon {

	final Set<RDFNode> licences = new HashSet<RDFNode>();

	public SpecCommon(Set<RDFNode> inheritedLicences, Resource root) {
		this(root);
		licences.addAll(inheritedLicences);
	}
	
	public SpecCommon(Resource root) {
		for (RDFNode x: root.listProperties(ELDA_API.license).mapWith(Statement.Util.getObject).toList()) {
			licences.add(x);
		}
	}
	
	public Set<RDFNode> getLicenceNodes() {
		return new HashSet<RDFNode>(licences);
	}
}
