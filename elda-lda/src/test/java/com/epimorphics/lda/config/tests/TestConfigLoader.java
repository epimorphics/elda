package com.epimorphics.lda.config.tests;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.configs.ConfigLoader;
import com.hp.hpl.jena.rdf.model.Model;

public class TestConfigLoader {

	@Test public void testConfigLoader() {
		Model m = ConfigLoader.loadModelExpanding("includefiles/toplevel.ttl");
		m.write(System.err, "TTL");
	}
}
