package com.epimorphics.lda.shortnames;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class BuiltIn {

	public static final List<Model> vocabularies = buildVocabularies();

	public static ArrayList<Model> buildVocabularies() {
		ArrayList<Model> result = new ArrayList<Model>();
//		FileManager.get().loadModel( "src/main/resources/builtins/random.ttl" );
		result.add( rdfModel() );
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

}
