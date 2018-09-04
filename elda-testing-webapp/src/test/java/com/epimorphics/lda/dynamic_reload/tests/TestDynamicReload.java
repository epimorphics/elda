package com.epimorphics.lda.dynamic_reload.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.lda.restlets.RouterRestlet;
import com.epimorphics.lda.systemtest.Util;
import com.epimorphics.lda.testing.utils.TomcatTestBase;

/**
	Test that tweaking a file will cause a reload of the configs.
*/
public class TestDynamicReload extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/main/webapp";
	}

	@Before public void beforeTesting() {
		RouterRestlet.refreshIntervalTestingMillis = 1000;
	}
	
	static final boolean runTests = 
		"true".equals(System.getenv("ELDA_TEST_DYNAMIC_RELOAD"));
	
	@After public void afterTesting() {
		RouterRestlet.refreshIntervalTestingMillis = 0;
	}
	
	@Test public void testDynamicReload() throws ClientProtocolException, IOException, InterruptedException {
		if (runTests) testDynamicReload("elda-config.ttl");
	}
	
	@Test public void testIncludeDynamicReload() throws ClientProtocolException, IOException, InterruptedException {
		if (runTests) testDynamicReload("empty.ttl");
	}

	private void testDynamicReload(String tweakPath) throws ClientProtocolException, IOException, InterruptedException {
		Util.testHttpRequest( "games", 200, Util.ignore );
		
		int lastNumber = RouterRestlet.loadCounter;
		tweak(tweakPath);

		Thread.sleep(1000);
		Util.testHttpRequest( "games", 200, Util.ignore );
		
		if(RouterRestlet.loadCounter <= lastNumber) {
			fail("did not reload after tweak " + tweakPath + ": remained at " + RouterRestlet.loadCounter  );
		}
	}

	private void tweak(String filePath) {
		File file = new File("src/main/webapp", filePath);
		file.setLastModified(System.currentTimeMillis());		
	}
}
