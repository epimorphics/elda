/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        TestProblematicEncodings.java
    Created by:  Dave Reynolds
    Created on:  4 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.jsonrdf;

import static com.epimorphics.jsonrdf.TestEncoder.testEncoding;

import java.io.IOException;

import org.junit.Test;
/**
 * Collection of cases that didn't work at first.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class TestProblematicEncodings {

    @Test
    public void testNameClash1() throws IOException {
        String srcTTL = ":school a :Shool; alt:type 'Primary'.";
        String[] roots = new String[]{":school"};
        String expectedEncoding = "[{'alt_type':'Primary','type':'http://www.epimorphics.com/tools/example#Shool','_about':'http://www.epimorphics.com/tools/example#school'}]";
        Context context = new Context();
        Encoder enc = Encoder.get(context);
        testEncoding(srcTTL, enc, context, roots, expectedEncoding);
    }
    
    @Test
    public void testNullLists() throws IOException {
    	Context context1 = new Context();
        testEncoding(":r :p [] .", 
                Encoder.get(context1), context1,
                new String[]{":r"}, 
                "[{'_about':'http://www.epimorphics.com/tools/example#r','p':{}}]" );
        Context context2 = new Context();
        testEncoding(":r :p () .", 
                Encoder.get(context2), context2,
                new String[]{":r"}, 
                "[{'_about':'http://www.epimorphics.com/tools/example#r','p':[]}]" );
    }
    
}

