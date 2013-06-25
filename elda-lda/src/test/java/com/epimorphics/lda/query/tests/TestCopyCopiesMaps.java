package com.epimorphics.lda.query.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.tests.SNS;

public class TestCopyCopiesMaps {

	@Test public void ensure_copy_copies_languages_for() {
		ShortnameService sns = new SNS( "" );
		APIQuery q1 = QueryTestUtils.queryFromSNS(sns);
		q1.setLanguagesFor( "p", "en,cy" );
		APIQuery q2 = new APIQuery( q1 );
		assertEquals( "en,cy", q2.languagesFor( "p" ) );
		q2.setLanguagesFor( "p", "fr" );
		assertEquals( "en,cy", q1.languagesFor( "p" ) );
		assertEquals( "fr", q2.languagesFor( "p" ) );
	}
}
