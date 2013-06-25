package com.epimorphics.lda.views.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.core.View;

public class TestCombiningViews {
	
	@Test public void ensureCopyPreservesType() {
		ensureCopyPreservesType( "View.ALL", View.ALL );
		ensureCopyPreservesType( "View.BASIC", View.BASIC );
		ensureCopyPreservesType( "View.DESCRIBE", View.DESCRIBE );
	}

	private void ensureCopyPreservesType( String tag, View v ) {
		assertEquals( "Copying " + tag + " should preserve type", v.getType(), v.copy().getType() );
	}

}
