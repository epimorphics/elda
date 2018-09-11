package com.epimorphics.lda.metadata.tests;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.metadata.MetaConfig;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.RDFUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

public class TestParseMetadataConfig {

	static final String apiPrefixString = "@prefix api: <http://purl.org/linked-data/api/vocab#>.";
	static final String rdfPrefixString = "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n";
	static final String rdfsPrefixString = "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n";
	static final String eldaPrefixString = "@prefix elda: <http://www.epimorphics.com/vocabularies/lda#> .\n";
	
	private Model getModelA(String value) {
		String modelString = longString(
			apiPrefixString
			, rdfPrefixString
			, rdfsPrefixString
			, eldaPrefixString
			, ":spec a api:API"
			, "; elda:disable-default-metadata VALUE".replace("VALUE", value)
			);
		return ModelIOUtils.modelFromTurtle(modelString);
	}
	
	public static String longString( String ... strings) {
		StringBuilder sb = new StringBuilder();
		for (String s: strings) {
			sb.append(s).append("\n");
		}			
		return sb.toString();
	}

	public MetaConfig parseMetaConfig(Model config) {
		List<Statement> apiStatements = config.listStatements(null, RDF.type, API.API).toList();
		Resource root = apiStatements.iterator().next().getSubject();
		boolean defaultMetadata = RDFUtils.getBooleanValue(root, ELDA_API.disable_default_metadata, false);
		return new MetaConfig(defaultMetadata);
	}
	
	@Test public void testDefaultFalse() {
		Model config = getModelA("false");
		MetaConfig mc = parseMetaConfig(config);
		assertEquals(false, mc.disableDefaultMetadata());
	}
	
	@Test public void testDefaultTrue() {
		Model config = getModelA("true");
		MetaConfig mc = parseMetaConfig(config);
		assertEquals(true, mc.disableDefaultMetadata());
	}
}
