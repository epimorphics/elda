package com.epimorphics.lda.testing.tomcat;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.testing.utils.TomcatTestBase;

public class TestSomething extends TomcatTestBase {

	@Override public String getWebappRoot() {
		return "src/test/testWebapp";
	}
	
	@Test public void whoops() {
		System.err.println(">> BOOMITY BOOM");
		fail("BOOM");
	}

}
