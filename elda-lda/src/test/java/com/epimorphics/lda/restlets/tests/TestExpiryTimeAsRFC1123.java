package com.epimorphics.lda.restlets.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.lda.restlets.RouterRestletSupport;

/**
    Sanity checks on converting dates to RDF1123. Match against gold 
    rather than anything clever.
*/
public class TestExpiryTimeAsRFC1123 {

	static class QandA {
		
		final long when;
		final String date;
		
		QandA(long when, String date) {
			this.when = when;
			this.date = date;
		}
	}
	
	static final long dayMillis = 1000L * 60 * 60 * 24;
	static final long yearMillis = dayMillis * 365;
	
	static final QandA [] tests = new QandA[] {
		new QandA(0L, "Thu, 01 Jan 1970 00:00:00 GMT" )
		, new QandA(dayMillis, "Fri, 02 Jan 1970 00:00:00 GMT" )
		, new QandA(dayMillis + 1000, "Fri, 02 Jan 1970 00:00:01 GMT" )
		, new QandA(dayMillis + 1000 * 60, "Fri, 02 Jan 1970 00:01:00 GMT" )
		, new QandA(yearMillis, "Fri, 01 Jan 1971 00:00:00 GMT" )
		, new QandA(yearMillis, "Fri, 01 Jan 1971 00:00:00 GMT" )
		, new QandA(10 * yearMillis, "Sun, 30 Dec 1979 00:00:00 GMT" )
	};
	
	@Test public void runTests() {
		
		for (QandA t: tests) {
			String when = RouterRestletSupport.expiresAtAsRFC1123(t.when);
			assertEquals(t.date, when);
		}
	}
}
