package com.epimorphics.lda.query.tests;

import junit.framework.Assert;

import org.junit.Test;

import com.epimorphics.lda.query.QueryParameter;

public class TestCallbackPatterns {
	
	@Test public void testThem() {
		expect( true, "plain" );
		expect( true, "_plain" );
		expect( true, "pla_in" );
		expect( true, "plain_" );
		expect( true, "p2l3ai_n3" );
		expect( false, "1plain" );
		expect( false, "pla+.in" );
		expect( false, "pl==ain" );
		expect( true, "quentin" );
		expect( true, "HodgePodge" );
		expect( true, "DARK_IN_HERE" );
	}

	private void expect(boolean expected, String string) {
		boolean b = QueryParameter.callbackPattern.matcher( string ).matches();
		if (b != expected) Assert.fail
			(string 
			+ " should" + (expected ? "" : " not") 
			+ " be allowed as a callback name." 
			);
	}

}
