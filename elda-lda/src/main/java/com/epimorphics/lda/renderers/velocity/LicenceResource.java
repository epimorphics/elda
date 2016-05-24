package com.epimorphics.lda.renderers.velocity;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class LicenceResource  {

	/**
	    Given a set of Resources of licences, return a set of LicenceResources
	    which know how to get the URI, label, and picture for that
	    resource.
	*/
    public static Set<LicenceResource> revise(Set<Resource> licences) {
    	Set<LicenceResource> result = new HashSet<LicenceResource>();
    	for (Resource r: licences) {
			result.add(new LicenceResource(r));
		}
    	return result;
	}

    final Resource wrapped;

	public LicenceResource(Resource toWrap) {
		this.wrapped = toWrap;
	}
	
	public String getURI() {
		return wrapped.getURI();
	}

	public String getLabel() {
		Statement label = wrapped.getProperty(RDFS.label);
		return label == null ? wrapped.getLocalName() : label.getObject().toString();
	}
	
	public String getPicture() {
		Statement picture = wrapped.getProperty(FOAF.depiction);
		return picture == null ? null : picture.getObject().toString();
	}
	
	public String toString() {
		return "{licence " + wrapped.toString() + "}";
	}
}