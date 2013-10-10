package com.epimorphics.lda.shortnames;

import java.util.*;

import com.epimorphics.lda.vocabularies.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.*;

public class BuiltIn {

	public static final List<Model> vocabularies = buildVocabularies();

	public static ArrayList<Model> buildVocabularies() {
		ArrayList<Model> result = new ArrayList<Model>();
//		EldaFileManager.get().loadModel( "src/main/resources/builtins/random.ttl" );
		result.add( rdfModel() );
		result.add( xsltModel() );
		return result;
	}

	private static Model rdfModel() {
		Model result = ModelFactory.createDefaultModel();
		propertyShortname(RDF.type.inModel(result))
			// .addProperty(RDF.type, API.Multivalued)
			.addProperty(RDFS.range, RDFS.Resource)
			;
		propertyShortname(RDFS.label.inModel(result))
			// mini.addProperty(RDF.type, API.Multivalued)
			;
		propertyShortname(RDFS.comment.inModel(result))
//			.addProperty(RDF.type, API.Multivalued)
			;
		propertyShortname( RDF.value.inModel(result) );
	//
		classShortname( XSD.integer.inModel(result) );
		classShortname( XSD.decimal.inModel(result) );
		classShortname( XSD.xstring.inModel(result) );
		classShortname( XSD.xboolean.inModel(result) );
		classShortname( XSD.xint.inModel(result) );
		classShortname( XSD.xshort.inModel(result) );
		classShortname( XSD.xbyte.inModel(result) );
		classShortname( XSD.xlong.inModel(result) );
		classShortname( XSD.xdouble.inModel(result) );
		classShortname( XSD.date.inModel(result) );
		classShortname( XSD.time.inModel(result) );
		return result;
	}
	
	private static void classShortname(Resource r) {
		r.addProperty(RDF.type, RDFS.Class );
		r.addProperty(RDFS.label, r.getLocalName() );
	}

	private static Resource propertyShortname(Property p) {
		return p
			.addProperty(RDF.type,  RDF.Property)
			.addProperty(RDFS.label, p.getLocalName() )
		;
	}
	
	private static Model xsltModel() {
		Model result = ModelFactory.createDefaultModel();
		for (Property p: magicURIs()) propertyShortname(p.inModel(result));
		
		propertyShortname( ELDA.DOAP_EXTRAS._implements.inModel(result) );
		propertyShortname( ELDA.DOAP_EXTRAS.releaseOf.inModel(result) );
		propertyShortname( DOAP.homepage.inModel(result) );
		propertyShortname( DOAP.repository.inModel(result) );
		propertyShortname( DOAP.browse.inModel(result) );
		propertyShortname( DOAP.location.inModel(result) );
		propertyShortname( DOAP.wiki.inModel(result) );
		propertyShortname( DOAP.revision.inModel(result) );
		
		definedShortname( DOAP.bug_database.inModel(result), "bug_database" );
		definedShortname( DOAP.programming_language.inModel(result), "programming_language" );
		
		propertyShortname( ELDA.COMMON.software.inModel(result) );
		
		return result;
	}		
	
	private static void definedShortname( Property p, String shortName ) {
		p
			.addProperty(RDF.type, RDF.Property)
			.addProperty(RDFS.label, shortName)
			;
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
