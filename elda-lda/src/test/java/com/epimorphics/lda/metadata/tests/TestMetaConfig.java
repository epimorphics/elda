package com.epimorphics.lda.metadata.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.EndpointMetadata;
import com.epimorphics.lda.metadata.MetaConfig;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.test.ModelTestBase;

public class TestMetaConfig {

	@Test public void testEmptyConfig() {
		assertEquals(false, new MetaConfig().disableDefaultMetadata());
		assertEquals(false, new MetaConfig(false).disableDefaultMetadata());
		assertEquals(true, new MetaConfig(true).disableDefaultMetadata());

		for (Property p: EndpointMetadata.hardwiredProperties)
			assertEquals(false, new MetaConfig(false).drop(p));
		
		for (Property p: EndpointMetadata.hardwiredProperties)
			assertEquals(true, new MetaConfig(true).drop(p));
		
		for (Property p: EndpointMetadata.hardwiredProperties)
			assertEquals(false, new MetaConfig().drop(p));
	}
	
	protected String configString(String control) {
		return TestParseMetadataConfig.longString
			( TestParseMetadataConfig.apiPrefixString
			, TestParseMetadataConfig.eldaPrefixString
			, TestParseMetadataConfig.rdfPrefixString
			, TestParseMetadataConfig.rdfsPrefixString
			, "@prefix : <eh:/> ."
			, ""
			, "<eh:/root> a api:API"
			, control
			, "; elda:enable-default-metadata ("
			, "<http://purl.org/dc/terms/hasPart>"
			, "<http://purl.org/dc/terms/isPartOf>"
			, ")"
			, "."
			);
	}
	
	@Test public void testEnablePropertiesConfigAbsent() {
		testEnablePropertiesConfig(false, false, "");
	}
		
	@Test public void testEnablePropertiesConfigFalse() {
		testEnablePropertiesConfig(false, false, "; elda:disable-default-metadata false");
	}
		
	@Test public void testEnablePropertiesConfigTrue() {
		testEnablePropertiesConfig(false, true, "; elda:disable-default-metadata true");
	}
		
	public void testEnablePropertiesConfig(boolean expectHardwired, boolean expectNotwired, String control) {
		Model config = ModelIOUtils.modelFromTurtle(configString(control));
		Resource root = config.createResource("eh:/root");
		MetaConfig mc = new MetaConfig(root);
		assertEquals(expectHardwired, mc.drop(config.createProperty("http://purl.org/dc/terms/hasPart")));
		assertEquals(expectNotwired, mc.drop(config.createProperty("eh:/not-at-all")));
	}
	protected String configNamedBlockNoSubst() {
		return TestParseMetadataConfig.longString
			( TestParseMetadataConfig.apiPrefixString
			, TestParseMetadataConfig.eldaPrefixString
			, TestParseMetadataConfig.rdfPrefixString
			, TestParseMetadataConfig.rdfsPrefixString
			, "@prefix : <eh:/> ."
			, ""
			, "<eh:/root> a api:API"
			, "; elda:metadata ["
			, "    api:name \"NameX\""
			, "    ; <eh:/P> 10"
			, "    ; <eh:/Q> <eh:/O>"
			, "    ]"
			, "."
			);
	}
	
	@Test public void testBuildMetadataBlockNoSubst() {
		Bindings b = new Bindings();
		Model config = ModelIOUtils.modelFromTurtle(configNamedBlockNoSubst());
		Resource root = config.createResource("eh:/root");
		
		MetaConfig mc = new MetaConfig(root);
		
		Model meta = ModelFactory.createDefaultModel();
		Resource metaRoot = meta.createResource("eh:/result");
		mc.addMetadata(metaRoot, b);
		
		String expectString = "<eh:/result> <eh:/P> 10; <eh:/Q> <eh:/O>.";
		Model expect = ModelIOUtils.modelFromTurtle(expectString);
		ModelTestBase.assertIsoModels("", expect, meta);
	}	
	
	protected String configNamedBlockWithSubst() {
		return TestParseMetadataConfig.longString
			( TestParseMetadataConfig.apiPrefixString
			, TestParseMetadataConfig.eldaPrefixString
			, TestParseMetadataConfig.rdfPrefixString
			, TestParseMetadataConfig.rdfsPrefixString
			, "@prefix : <eh:/> ."
			, ""
			, "<eh:/root> a api:API"
			, "; elda:metadata ["
			, "    api:name \"NameX\""
			, "    ; <eh:/R> \"A{B}C\""
			, "    ]"
			, "."
			);
	}
	
	@Test public void testBuildMetadataBlockWithSubst() {
		Bindings b = new Bindings().put("B", "X");
		Model config = ModelIOUtils.modelFromTurtle(configNamedBlockWithSubst());
		Resource root = config.createResource("eh:/root");
		
		MetaConfig mc = new MetaConfig(root);
		
		Model meta = ModelFactory.createDefaultModel();
		Resource metaRoot = meta.createResource("eh:/result");
		mc.addMetadata(metaRoot, b);
		
		String expectString = "<eh:/result> <eh:/R> \"AXC\".";
		Model expect = ModelIOUtils.modelFromTurtle(expectString);
		ModelTestBase.assertIsoModels("", expect, meta);
	}
	
	protected String configNamedBlockWithResource() {
		return TestParseMetadataConfig.longString
			( TestParseMetadataConfig.apiPrefixString
			, TestParseMetadataConfig.eldaPrefixString
			, TestParseMetadataConfig.rdfPrefixString
			, TestParseMetadataConfig.rdfsPrefixString
			, "@prefix : <eh:/> ."
			, ""
			, "<eh:/root> a api:API"
			, "; elda:metadata ["
			, "    api:name \"NameX\""
			, "    ; <eh:/R> \"eh:/{B}\"^^rdfs:Resource"
			, "    ]"
			, "."
			);
	}
	
	@Test public void testBuildMetadataBlockWithResource() {
		Bindings b = new Bindings().put("B", "X");
		Model config = ModelIOUtils.modelFromTurtle(configNamedBlockWithResource());
		Resource root = config.createResource("eh:/root");
		
		MetaConfig mc = new MetaConfig(root);
		
		Model meta = ModelFactory.createDefaultModel();
		Resource metaRoot = meta.createResource("eh:/result");
		mc.addMetadata(metaRoot, b);
		
		String expectString = "<eh:/result> <eh:/R> <eh:/X>.";
		Model expect = ModelIOUtils.modelFromTurtle(expectString);
		ModelTestBase.assertIsoModels("", expect, meta);
	}
	protected String configErrorReusedName() {
		return TestParseMetadataConfig.longString
			( TestParseMetadataConfig.apiPrefixString
			, TestParseMetadataConfig.eldaPrefixString
			, TestParseMetadataConfig.rdfPrefixString
			, TestParseMetadataConfig.rdfsPrefixString
			, "@prefix : <eh:/> ."
			, ""
			, "<eh:/root> a api:API"
			, "; elda:metadata ["
			, "    api:name \"REPEATED\""
			, "    ; <eh:/R> \"eh:/{B}\"^^rdfs:Resource"
			, "    ]"
			, "    , [api:name \"REPEATED\"]"
			, "."
			);
	}
	
	@Test public void testBuildMetadataBlockError() {
		Bindings b = new Bindings().put("B", "X");
		Model config = ModelIOUtils.modelFromTurtle(configErrorReusedName());
		Resource root = config.createResource("eh:/root");
		
		try {

			MetaConfig mc = new MetaConfig(root);
		
			Model meta = ModelFactory.createDefaultModel();
			Resource metaRoot = meta.createResource("eh:/result");

			mc.addMetadata(metaRoot, b);
			fail("should have detected reused name");
		} catch (RuntimeException e) {
			// As expected.
		}
		

	}
		
		
}
