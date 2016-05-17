package com.epimorphics.lda.specs;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.*;

public class SpecCommon {

	final Set<RDFNode> licenceProperties = new HashSet<RDFNode>();

	public SpecCommon(Set<RDFNode> inheritedLicenceProperties, Resource root) {
		this(root);
		licenceProperties.addAll(inheritedLicenceProperties);
	}
	
	public SpecCommon(Resource root) {
		for (RDFNode x: root.listProperties(ELDA_API.license).mapWith(Statement.Util.getObject).toList()) {
			licenceProperties.add(x);
		}
	}
	
	public Set<RDFNode> getLicenceNodes() {
		return new HashSet<RDFNode>(licenceProperties);
	}
}
