/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.prefixes;

import org.apache.jena.shared.PrefixMapping;

/**
 	<p>
    A bunch of prefixes for Elda to use when incoming URIs have namespaces
    not defined by the config.
    </p>

	<p>
	These were the top 100 prefixes on prefix.cc on 11th March 2013.
	</p>

*/
public class Prefixes {
	
	public static final PrefixMapping various = PrefixMapping.Factory.create()
		.setNsPrefix("ad", "http://schemas.talis.com/2005/address/schema#")
		.setNsPrefix("af", "http://purl.org/ontology/af/")
		.setNsPrefix("afn", "http://jena.hpl.hp.com/ARQ/function#")
		.setNsPrefix("aiiso", "http://purl.org/vocab/aiiso/schema#")
		.setNsPrefix("air", "http://dig.csail.mit.edu/TAMI/2007/amord/air#")
		.setNsPrefix("akt", "http://www.aktors.org/ontology/portal#")
		.setNsPrefix("biblio", "http://purl.org/net/biblio#")
		.setNsPrefix("bibo", "http://purl.org/ontology/bibo/")
		.setNsPrefix("bill", "http://www.rdfabout.com/rdf/schema/usbill/")
		.setNsPrefix("bio", "http://purl.org/vocab/bio/0.1/")
		.setNsPrefix("book", "http://purl.org/NET/book/vocab#")
		.setNsPrefix("botany", "http://purl.org/NET/biol/botany#")
		.setNsPrefix("cal", "http://www.w3.org/2002/12/cal/ical#")
		.setNsPrefix("cc", "http://creativecommons.org/ns#")
		.setNsPrefix("cfp", "http://sw.deri.org/2005/08/conf/cfp.owl#")
		.setNsPrefix("cld", "http://purl.org/cld/terms/")
		.setNsPrefix("cmp", "http://www.ontologydesignpatterns.org/cp/owl/componency.owl#")
		.setNsPrefix("co", "http://purl.org/ontology/co/core#")
		.setNsPrefix("content", "http://purl.org/rss/1.0/modules/content/")
		.setNsPrefix("cs", "http://purl.org/vocab/changeset/schema#")
		.setNsPrefix("ctag", "http://commontag.org/ns#")
		.setNsPrefix("cv", "http://purl.org/captsolo/resume-rdf/0.2/cv#")
		.setNsPrefix("d2rq", "http://www.wiwiss.fu-berlin.de/suhl/bizer/D2RQ/0.1#")
		.setNsPrefix("daia", "http://purl.org/ontology/daia/")
		.setNsPrefix("days", "http://ontologi.es/days#")
		.setNsPrefix("dbo", "http://dbpedia.org/ontology/")
		.setNsPrefix("dbpedia", "http://dbpedia.org/resource/")
		.setNsPrefix("dbp", "http://dbpedia.org/property/")
		.setNsPrefix("dbpprop", "http://dbpedia.org/property/")
		.setNsPrefix("dbr", "http://dbpedia.org/resource/")
		.setNsPrefix("dc11", "http://purl.org/dc/elements/1.1/")
		.setNsPrefix("dcmit", "http://purl.org/dc/dcmitype/")
		.setNsPrefix("dcn", "http://www.w3.org/2007/uwa/context/deliverycontext.owl#")
		.setNsPrefix("dcq", "http://purl.org/dc/terms/")
		.setNsPrefix("dcterms", "http://purl.org/dc/terms/")
		.setNsPrefix("dct", "http://purl.org/dc/terms/")
		.setNsPrefix("dir", "http://schemas.talis.com/2005/dir/schema#")
		.setNsPrefix("doap", "http://usefulinc.com/ns/doap#")
		.setNsPrefix("earl", "http://www.w3.org/ns/earl#")
		.setNsPrefix("event", "http://purl.org/NET/c4dm/event.owl#")
		.setNsPrefix("ex", "http://example.com/")
		.setNsPrefix("factbook", "http://www4.wiwiss.fu-berlin.de/factbook/ns#")
		.setNsPrefix("fb", "http://rdf.freebase.com/ns/")
		.setNsPrefix("fn", "http://www.w3.org/2005/xpath-functions#")
		.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/")
		.setNsPrefix("gen", "http://www.w3.org/2006/gen/ont#")
		.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#")
		.setNsPrefix("geonames", "http://www.geonames.org/ontology#")
		.setNsPrefix("giving", "http://ontologi.es/giving#")
		.setNsPrefix("gr", "http://purl.org/goodrelations/v1#")
		.setNsPrefix("http", "http://www.w3.org/2006/http#")
		.setNsPrefix("ical", "http://www.w3.org/2002/12/cal/ical#")
		.setNsPrefix("ir", "http://www.ontologydesignpatterns.org/cp/owl/informationrealization.owl#")
		.setNsPrefix("jdbc", "http://d2rq.org/terms/jdbc/")
		.setNsPrefix("log", "http://www.w3.org/2000/10/swap/log#")
		.setNsPrefix("lomvoc", "http://ltsc.ieee.org/rdf/lomv1p0/vocabulary#")
		.setNsPrefix("math", "http://www.w3.org/2000/10/swap/math#")
		.setNsPrefix("media", "http://purl.org/microformat/hmedia/")
		.setNsPrefix("memo", "http://ontologies.smile.deri.ie/2009/02/27/memo#")
		.setNsPrefix("mu", "http://www.kanzaki.com/ns/music#")
		.setNsPrefix("musim", "http://purl.org/ontology/similarity/")
		.setNsPrefix("myspace", "http://purl.org/ontology/myspace#")
		.setNsPrefix("nie", "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#")
		.setNsPrefix("ok", "http://okkam.org/terms#")
		.setNsPrefix("ome", "http://purl.org/ontomedia/core/expression#")
		.setNsPrefix("org", "http://www.w3.org/ns/org#")
		.setNsPrefix("osag", "http://www.ordnancesurvey.co.uk/ontology/AdministrativeGeography/v2.0/AdministrativeGeography.rdf#")
		.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#")
		.setNsPrefix("owlim", "http://www.ontotext.com/trree/owlim#")
		.setNsPrefix("rdfg", "http://www.w3.org/2004/03/trix/rdfg-1/")
		.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
		.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
		.setNsPrefix("reco", "http://purl.org/reco#")
		.setNsPrefix("rel", "http://purl.org/vocab/relationship/")
		.setNsPrefix("rev", "http://purl.org/stuff/rev#")
		.setNsPrefix("rif", "http://www.w3.org/2007/rif#")
		.setNsPrefix("rss", "http://purl.org/rss/1.0/")
		.setNsPrefix("sd", "http://www.w3.org/ns/sparql-service-description#")
		.setNsPrefix("sioc", "http://rdfs.org/sioc/ns#")
		.setNsPrefix("sism", "http://purl.oclc.org/NET/sism/0.1/")
		.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#")
		.setNsPrefix("sr", "http://www.openrdf.org/config/repository/sail#")
		.setNsPrefix("swande", "http://purl.org/swan/1.2/discourse-elements/")
		.setNsPrefix("swanq", "http://purl.org/swan/1.2/qualifiers/")
		.setNsPrefix("swc", "http://data.semanticweb.org/ns/swc/ontology#")
		.setNsPrefix("swrc", "http://swrc.ontoware.org/ontology#")
		.setNsPrefix("tag", "http://www.holygoat.co.uk/owl/redwood/0.1/tags/")
		.setNsPrefix("test2", "http://this.invalid/test2#")
		.setNsPrefix("tzont", "http://www.w3.org/2006/timezone#")
		.setNsPrefix("vann", "http://purl.org/vocab/vann/")
		.setNsPrefix("vcard", "http://www.w3.org/2006/vcard/ns#")
		.setNsPrefix("void", "http://rdfs.org/ns/void#")
		.setNsPrefix("wot", "http://xmlns.com/wot/0.1/")
		.setNsPrefix("xf", "http://www.w3.org/2002/xforms/")
		.setNsPrefix("xfn", "http://vocab.sindice.com/xfn#")
		.setNsPrefix("xhtml", "http://www.w3.org/1999/xhtml#")
		.setNsPrefix("xhv", "http://www.w3.org/1999/xhtml/vocab#")
		.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#")
		.setNsPrefix("xs", "http://www.w3.org/2001/XMLSchema#")
		;
}
