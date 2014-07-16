/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
 */

package com.epimorphics.lda.vocabularies;

import com.epimorphics.lda.Version;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
	Elda-specific vocabulary and metadata.
*/
public class ELDA {
	static final String version = Version.string;

	public static final String tag = "";

	static final private Model m = ModelFactory.createDefaultModel();

	static private Resource resource(String NS, String local) {
		return m.createResource(NS + local);
	}

	static private Property property(String NS, String local) {
		return m.createProperty(NS + local);
	}

	public static final Resource Elda = m.createResource();

	public static final Resource ThisElda = resource(ELDA_API.NS, "Elda_" + version);

	public static final Resource EldaRepository = resource("https://github.com/epimorphics/elda.git", "");

	public static class DOAP_EXTRAS {
		static final String NS = DOAP.NS;

		public static final Property releaseOf = property(DOAP_EXTRAS.NS, "releaseOf");
		public static final Property _implements = property(DOAP_EXTRAS.NS, "implements");
	}

	public static class COMMON {
		public static final String NS = "http://purl.org/net/opmv/types/common#";

		public static final Property software = property(NS, "software");
	}

	/**
	 * Add the Elda processor metadata to the given resource <code>P</code>.
	 */
	public static void addEldaMetadata(Resource P) {
		Model m = P.getModel();
		P.addProperty(RDF.type, API.Service).addProperty(COMMON.software, ThisElda);
		ThisElda.inModel(m)
			.addProperty(RDFS.label, "Elda " + version + tag)
			.addProperty(RDF.type, DOAP.Version)
			.addProperty(DOAP.revision, version)
			.addProperty(ELDA.DOAP_EXTRAS.releaseOf, Elda)
			;
		Elda.inModel(m)
			.addProperty(RDFS.label, "Elda")
			.addProperty(DOAP.homepage, m.createResource("https://github.com/epimorphics/elda"))
			.addProperty(DOAP.wiki, m.createResource("https://github.com/epimorphics/elda/wiki"))
			.addProperty(DOAP.bug_database, m.createResource("https://github.com/epimorphics/elda/issues?direction=desc&sort=created&state=open"))
			.addProperty(DOAP.programming_language, "Java").addProperty(DOAP.repository, EldaRepository)
			.addProperty(ELDA.DOAP_EXTRAS._implements, "http://code.google.com/p/linked-data-api/wiki/Specification")
			;
		EldaRepository.inModel(m).addProperty(RDF.type, DOAP.Repository).addProperty(DOAP.location, m.createResource("https://github.com/epimorphics/elda"))
			.addProperty(DOAP.browse, m.createResource("https://github.com/epimorphics/elda"))
			;
	}
}
