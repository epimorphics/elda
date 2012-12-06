package com.epimorphics.lda.shortnames;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class BuiltIn {

	public static final List<Model> vocabularies = buildVocabularies();

	public static ArrayList<Model> buildVocabularies() {
		ArrayList<Model> result = new ArrayList<Model>();
//		FileManager.get().loadModel( "src/main/resources/builtins/random.ttl" );
		result.add( rdfModel() );
		result.add( xsltModel() );
		return result;
	}

	private static Model rdfModel() {
		Model result = ModelFactory.createDefaultModel();
		RDF.type.inModel(result)
			.addProperty(RDF.type, RDF.Property)
			.addProperty(RDF.type, API.Multivalued)
			.addProperty(RDFS.label, "type")
			.addProperty(RDFS.range,  RDFS.Resource)
			;
		RDFS.label.inModel(result)
			.addProperty(RDF.type, RDF.Property)
			.addProperty(RDFS.label, "label")
			.addProperty(RDF.type, API.Multivalued)
			;
		return result;
	}
	
	private static Model xsltModel() {
		Model result = ModelFactory.createDefaultModel();
		for (String s: magicURIs()) {
			Property p = result.createProperty( s );
			p
				.addProperty( RDF.type, RDF.Property )
				.addProperty( RDFS.label,  p.getLocalName() );
		}
		return result;
	}		
	
	private static Set<String> magicURIs() {
		Set<String> magic = new HashSet<String>();
		magic.add( API.definition.getURI() );
		magic.add( API.extendedMetadataVersion.getURI() );
		magic.add( API.items.getURI() );
		magic.add( API.label.getURI() );
		magic.add( API.page.getURI() );
		magic.add( API.processor.getURI() );
		magic.add( API.property.getURI() );
		magic.add( API.selectionResult.getURI() );
		magic.add( API.termBinding.getURI() );
		magic.add( API.value.getURI() );
		magic.add( API.variableBinding.getURI() );
		magic.add( API.viewingResult.getURI() );
		magic.add( API.wasResultOf.getURI() );
		magic.add( DCTerms.format.getURI() );
		magic.add( DCTerms.hasFormat.getURI() );
		magic.add( DCTerms.hasPart.getURI() );
		magic.add( DCTerms.hasVersion.getURI() );
		magic.add( DCTerms.isFormatOf.getURI() );
		magic.add( DCTerms.isPartOf.getURI() );
		magic.add( DCTerms.isVersionOf.getURI() );
		magic.add( EXTRAS.listURL.getURI() );
		magic.add( EXTRAS.sparqlQuery.getURI() );
		magic.add( FOAF.isPrimaryTopicOf.getURI() );
		magic.add( FOAF.primaryTopic.getURI() );
		magic.add( OpenSearch.itemsPerPage.getURI() );
		magic.add( OpenSearch.startIndex.getURI() );
		magic.add( RDFS.comment.getURI() );
		magic.add( RDFS.label.getURI() );
		magic.add( RDF.type.getURI() );
		magic.add( RDF.value.getURI() );
		magic.add( SPARQL.endpoint.getURI() );
		magic.add( SPARQL.query.getURI() );
		magic.add( SPARQL.url.getURI() );
		magic.add( XHV.first.getURI() );
		magic.add( XHV.next.getURI() );
		magic.add( XHV.prev.getURI() );
		return magic;
	}

}
