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

import java.io.StringReader;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.test.ModelTestBase;
/**
 * Test cases the encode to JSONObject cases
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class TestEncodeToObject {

    @Test public void testModelEncode() {
        Model src = ModelIOUtils.modelFromTurtle(":r :p 42; :q :r2. :r2 :p 24 .");
        Context context = new Context();
        JsonObject obj = Encoder.get(context).encode(src);
        String encoding = obj.toString();
        Model dec = Decoder.decodeModel(context, new StringReader(encoding) );
        ModelTestBase.assertIsoModels(src, dec);
    }
}

