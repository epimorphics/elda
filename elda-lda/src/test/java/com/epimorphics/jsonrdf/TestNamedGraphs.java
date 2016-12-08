/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        TestNamedGraphs.java
    Created by:  Dave Reynolds
    Created on:  29 Dec 2009
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
 * THE SOFTWARE.
 * $Id:  $
 *****************************************************************/

package com.epimorphics.jsonrdf;

import static com.epimorphics.jsonrdf.utils.ModelIOUtils.modelFromTurtle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.*;

import org.apache.jena.atlas.json.JsonException;
import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelCompareUtils;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Test the round tripping of named graphs
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class TestNamedGraphs {

    public void testNamedGraphs(String def, String[] names, String[] graphs) throws IOException, JsonException {
        Model defM = modelFromTurtle(def);
        Context context = new Context(defM);
        Dataset source = DatasetFactory.create(defM);
        for (int i = 0; i < names.length; i++) {
            source.addNamedModel(names[i], ModelIOUtils.modelFromTurtle(graphs[i]));
        }
        StringWriter writer = new StringWriter();
        Encoder.get(context).encode(source, writer);
        String encoding = writer.toString();
//        System.out.println(encoding);
        
        StringReader reader = new StringReader( encoding );
        Dataset result = Decoder.decodeGraphs(context, reader);
        assertTrue("Check default model", result.getDefaultModel().isIsomorphicWith(defM));
        
        List<String> fetchedNameList = listFromIterator(result.listNames());
        Set<String> fetchedNames = new HashSet<String>(fetchedNameList);
        assertEquals("should be as many names fetched as were given", names.length, fetchedNameList.size());
        Set<String> givenNames = setFromArray(names);
        assertEquals("there should be the same names fetched as were given", givenNames, fetchedNames);        
        
        for (Iterator<String> ni = result.listNames(); ni.hasNext(); ) {
            String name = ni.next();
            
            Model model = result.getNamedModel(name);
            Model expectedModel = source.getNamedModel(name);
            
//            boolean match = model.isIsomorphicWith(expectedModel);
            boolean match = ModelCompareUtils.compareAndDisplayDifferences( expectedModel, model );
//            if (!match) {
//                System.out.println("Model " + name);
//                model.write(System.out, "Turtle");
//            }
            assertTrue("expected and found models must be isomorphic", match);
        }
    }
    
    private List<String> listFromIterator(Iterator<String> names) {
    	List<String> strings = new ArrayList<String>();
    	while (names.hasNext()) strings.add(names.next());
    	return strings;
	}

	private Set<String> setFromArray(String[] names) {
    	Set<String> strings = new HashSet<String>();
    	for (String name: names) strings.add(name);
    	return strings;
	}

	@Test public void testNamedGraphs() throws IOException, JsonException {
        testNamedGraphs(
                ":r :p 'foo'.", 
                new String[]{"http://www.epimoporphics.com/graph1", "http://www.epimoporphics.com/graph2"},
                new String[]{":r2 :p2 'foobar'.", ":r3 :p3 'foobarbaz'."} );
        testNamedGraphs(
                ":r :p 'foo'.", 
                new String[]{},
                new String[]{} );
        testNamedGraphs(
                ":r :p 'foo'.", 
                new String[]{"http://www.epimoporphics.com/graph1"},
                new String[]{":r2 :p2 'foobar'."} );
    }
    
}

