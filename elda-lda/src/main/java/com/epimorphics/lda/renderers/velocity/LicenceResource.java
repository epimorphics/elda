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
			final Resource r1 = r;
			result.add(new LicenceResource(r1));
		}
    	return result;
	}

    final Resource r;

	public LicenceResource(Resource r) {
		this.r = r;
	}
	
	public String getURI() {
		return r.getURI();
	}

	public String getLabel() {
		Statement label = r.getProperty(RDFS.label);
		return label == null ? r.getLocalName() : label.getObject().toString();
	}
	
	public String getPicture() {
		Statement picture = r.getProperty(FOAF.depiction);
		return picture == null ? null : picture.getObject().toString();
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		String depiction = getPicture();	
					
		if (depiction != null) 
			sb.append("<img src=\"").append(depiction).append("\"></img>");
		
		sb
			.append("<a href=\"")
			.append(r.getURI())
			.append("\">")
			.append(getLabel())
			.append("</a>")
			.append("\n")
			;
		
		return sb.toString();
	}
}