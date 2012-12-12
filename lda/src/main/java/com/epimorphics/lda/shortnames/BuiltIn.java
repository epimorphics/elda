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
import com.hp.hpl.jena.rdf.model.Resource;
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
		localShortname(RDF.type.inModel(result))
			.addProperty(RDF.type, API.Multivalued)
			.addProperty(RDFS.range, RDFS.Resource)
			;
		localShortname(RDFS.label.inModel(result))
			.addProperty(RDF.type, API.Multivalued)
			;
		localShortname(RDFS.comment.inModel(result))
			.addProperty(RDF.type, API.Multivalued)
			;
		localShortname( RDF.value.inModel(result) );
		return result;
	}
	
	private static Resource localShortname(Property p) {
		return p
			.addProperty(RDF.type,  RDF.Property)
			.addProperty(RDFS.label, p.getLocalName() )
		;
	}
	
	private static Model xsltModel() {
		Model result = ModelFactory.createDefaultModel();
		for (Property p: magicURIs()) localShortname(p.inModel(result));
		return result;
	}		
	
	private static Set<Property> magicURIs() {
		Set<Property> magic = new HashSet<Property>();
		magic.add( API.definition );
		magic.add( API.extendedMetadataVersion );
		magic.add( API.items );
		magic.add( API.page );
		magic.add( API.processor );
		magic.add( API.property );
		magic.add( API.selectionResult );
		magic.add( API.termBinding );
		magic.add( API.variableBinding );
		magic.add( API.viewingResult );
		magic.add( API.wasResultOf );
		magic.add( DCTerms.format );
		magic.add( DCTerms.hasFormat );
		magic.add( DCTerms.hasPart );
		magic.add( DCTerms.hasVersion );
		magic.add( DCTerms.isFormatOf );
		magic.add( DCTerms.isPartOf );
		magic.add( DCTerms.isVersionOf );
		magic.add( EXTRAS.listURL );
		magic.add( EXTRAS.sparqlQuery );
		magic.add( FOAF.isPrimaryTopicOf );
		magic.add( FOAF.primaryTopic );
		magic.add( OpenSearch.itemsPerPage );
		magic.add( OpenSearch.startIndex );
		magic.add( SPARQL.endpoint );
		magic.add( SPARQL.query );
		magic.add( SPARQL.url );
		magic.add( XHV.first );
		magic.add( XHV.next );
		magic.add( XHV.prev );
		return magic;
	}

}
