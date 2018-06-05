package com.epimorphics.lda.config.tests;

import org.junit.Test;

import com.epimorphics.lda.configs.ConfigLoader;

public class TestConfigLoader {

	@Test public void testConfigLoader() {
		ConfigLoader.loadModelExpanding("includefiles/toplevel.ttl");
	}
}
