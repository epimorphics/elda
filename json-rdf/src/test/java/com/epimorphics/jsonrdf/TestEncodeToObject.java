/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
 File:        TestEncodeToObject.java
 Created by:  Dave Reynolds
 Created on:  3 Feb 2010
 *
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.jsonrdf;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Test cases the encode to JSONObject cases
 *
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class TestEncodeToObject {

    static {
        System.err.println("TestEncodeToObject suppressed");
    }

    @Test
    public void testModelEncode() {
        if (true) return;

        Model src = ModelIOUtils.modelFromTurtle(":r :p 42; :q :r2. :r2 :p 24 .");
        JsonObject obj = Encoder.get().encode(src);
        String encoding = obj.toString();

        System.err.println(">> encoding:\n" + encoding);

        Model dec = Decoder.decodeModel(new StringReader(encoding));

        if (dec.isIsomorphicWith(src)) {

        } else {
            dec.setNsPrefixes(src.getNsPrefixMap());
            System.err.println(">> Expected:");
            src.write(System.err, "TTL");
            System.err.println(">> Obtained:");
            dec.write(System.err, "TTL");
            fail("Round-trip failure");
        }
//        assertTrue( dec.isIsomorphicWith(src) );
    }
}
