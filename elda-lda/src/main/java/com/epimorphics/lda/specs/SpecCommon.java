package com.epimorphics.lda.specs;

import com.epimorphics.lda.vocabularies.ELDA_API;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.util.HashSet;
import java.util.Set;

public class SpecCommon {

    final Resource root;

    final Set<RDFNode> licences = new HashSet<RDFNode>();

    public SpecCommon(Set<RDFNode> inheritedLicences, Resource root) {
        this(root);
        licences.addAll(inheritedLicences);
    }

    public SpecCommon(Resource root) {
        this.root = root;
        for (RDFNode x : root.listProperties(ELDA_API.license).mapWith(Statement::getObject).toList()) {
            licences.add(x);
        }
    }

    public Set<RDFNode> getLicenceNodes() {
        return new HashSet<RDFNode>(licences);
    }

    public Set<Resource> getNotices() {
        Set<Resource> result = new HashSet<Resource>();
        for (RDFNode x : root.listProperties(ELDA_API.notice).mapWith(Statement::getObject).toList()) {
            result.add(x.asResource());
        }
        return result;
    }
}
