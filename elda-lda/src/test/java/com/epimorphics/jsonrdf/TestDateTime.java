/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        TestDateTime.java
    Created by:  Dave Reynolds
    Created on:  28 Dec 2009
 * 
 * (c) Copyright 2011 Epimorphics Limited
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE. * $Id:  $
 *****************************************************************/

package com.epimorphics.jsonrdf;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class TestDateTime {
	
//	@Test public void testNoTimeZone() {
//		Literal l = ResourceFactory.createTypedLiteral("1999-05-31T02:09:32", XSDDatatype.XSDdateTime);
//		System.err.println( ">> " + l );
//		XSDDateTime t = (XSDDateTime) l.getValue();
//		System.err.println( ">> " + t );
//		// EEE, d MMM yyyy HH:mm:ss 'GMT'Z
//		System.err.println( ">> " + t.getDays() );
//		System.err.println( ">> " + t.getFullSeconds() );
//		System.err.println( ">> " + t.getHours() );
//		System.err.println( ">> " + t.getMinutes() );
//		System.err.println( ">> " + t.getMonths() );
//		System.err.println( ">> " + t.getSeconds() );
//		System.err.println( ">> " + t.getYears() );
//		t.
//		
//	}

    @Test public void testBasicSerialization() {
        Literal l = ResourceFactory.createTypedLiteral("1999-05-31T02:09:32Z", XSDDatatype.XSDdateTime);
        assertEquals("Mon, 31 May 1999 02:09:32 GMT+0000", RDFUtil.formatDateTime(l));
    }
    
    @Test public void testRoundTrip() throws ParseException {
        Literal l = ResourceFactory.createTypedLiteral("1999-05-31T02:09:32Z", XSDDatatype.XSDdateTime);
        String date = RDFUtil.formatDateTime(l);
        Literal lret = RDFUtil.parseDateTime(date, null);
        assertEquals(l, lret);
    }
    
    @Test public void testRoundTripDate() throws ParseException {
        Literal l = ResourceFactory.createTypedLiteral("1999-05-31", XSDDatatype.XSDdate);
        String date = RDFUtil.formatDateTime(l);
        Literal lret = RDFUtil.parseDateTime(date, l.getDatatypeURI() );
        assertEquals(l, lret);
    }
    
    @Test public void testTimelessDatetimeRendering() {
        Literal l = ResourceFactory.createTypedLiteral("1999-05-31Z", XSDDatatype.XSDdate);
    	assertEquals( "Mon, 31 May 1999", RDFUtil.formatDateTime( l ) );
    }
}

