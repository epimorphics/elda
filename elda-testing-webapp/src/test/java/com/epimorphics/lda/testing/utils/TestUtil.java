/******************************************************************
 * File:        TestUtil.java
 * Created by:  Dave Reynolds
 * Created on:  30 Nov 2011
 *
 * (c) Copyright 2011, Epimorphics Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *****************************************************************/

package com.epimorphics.lda.testing.utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.Map1;

/**
 * Support for testing iterator/list values against and expected set
 * of answers.
 *
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class TestUtil {

    public static <E> void testArray(E[] actual, E[] expected) {
        Set<Object> expectedSet = new HashSet<Object>();
        for (Object e : expected) expectedSet.add(e);

        Set<Object> actualSet = new HashSet<Object>();
        for (Object a : actual) actualSet.add(a);

        assertEquals(expectedSet, actualSet);
    }

    public static <E> void testArray(Collection<E> actual, E[] expected) {
        Set<Object> expectedSet = new HashSet<Object>();
        for (Object e : expected) expectedSet.add(e);

        Set<Object> actualSet = new HashSet<Object>( actual );

        assertEquals(expectedSet, actualSet);
    }

    public static <E> void testArray(Iterator<E> actual, E[] expected) {
        Set<Object> expectedSet = new HashSet<Object>();
        for (Object e : expected) expectedSet.add(e);

        Set<Object> actualSet = new HashSet<Object>( );
        while (actual.hasNext()) {
            actualSet.add(actual.next());
        }

        assertEquals(expectedSet, actualSet);
    }

    /**
     * Create a {@link Model} as a text fixture
     * @param content The model content in Turtle. Common prefixes may be assumed.
     * @return A new model containing triples parsed from the content
     */
    public static Model modelFixture( String content ) {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefixes( PrefixUtils.commonPrefixes() );
        m.setNsPrefix( "", baseURIFixture() );
        String withPrefixes = PrefixUtils.asTurtlePrefixes( m ) + content;
        m.read( new StringReader( withPrefixes ), baseURIFixture(), "Turtle" );
        return m;
    }

    /**
     * Return a resource with the given URI.
     * @param m Optional model. If null, the resource will be created using the {@link ResourceFactory}
     * @param uri Resource URI. If the URI starts with <code>http:</code>, it will be left intact otherwise
     * it is assumed relative to the {@link #baseURIFixture()}
     * @return A resource
     */
    public static Resource resourceFixture( Model m, String uri ) {
        String u = uri.startsWith( "http:" ) ? uri : (baseURIFixture() + uri);
        return (m == null) ? ResourceFactory.createResource( u ) : m.getResource( u );
    }

    /**
     * Return a resource which is the (assumed sole) root subject of the model given by the turtle source.
     * Common prefixes are assumed.
     */
    public static Resource resourceFixture( String src ) {
        return RDFUtil.findRoot( modelFixture(src) );
    }

    /**
     * Return a property with the given URI.
     * @param m Optional model. If null, the property will be created using the {@link ResourceFactory}
     * @param uri Resource URI. If the URI starts with <code>http:</code>, it will be left intact otherwise
     * it is assumed relative to the {@link #baseURIFixture()}
     * @return A property
     */
    public static Property propertyFixture( Model m, String uri ) {
        String u = uri.startsWith( "http:" ) ? uri : (baseURIFixture() + uri);
        return (m == null) ? ResourceFactory.createProperty( u ) : m.getProperty( u );
    }

    /**
     * Return a base URI that is guaranteed not to resolve.
     * @return "http://example.test/test#"
     */
    public static String baseURIFixture() {
        return "http://example.test/test#";
    }

    /**
     * Test that the given resource/property has the given object value and ONLY the given object value.
     * @param subject  subject resource
     * @param predicate property to test
     * @param object  the expected value or null to not test the value, just that there is one
     */
    public static boolean isOnlyValue(Resource subject, Property predicate, RDFNode object) {
        StmtIterator si = subject.listProperties(predicate);
        if (si.hasNext()) {
            RDFNode value = si.next().getObject();
            if (object != null && ! value.equals(object)) return false;
        }
        if (si.hasNext()) {
            si.close();
            return false;
        }
        return true;
    }

    /**
     * Compare the properties of two resources, omitting any of the list of blocked properties.
     * Single, unadorned, bNode values on the expected resource are treated as wild cards, the
     * actual is required to just have some value for that property.
     */
    public static void testResourcesMatch(Resource expected, Resource actual, Property... omit) {
        Set<Property> testProperties = propertyList(actual, omit);
        assertEquals(propertyList(expected, omit), testProperties);
        boolean ok = match(expected, actual, testProperties.toArray(new Property[testProperties.size()]));
        if (!ok) {
            for (Property p : testProperties) {
                testMatch(expected, actual, p);
            }
        }
    }

    private static Set<Property> propertyList(Resource r, Property... omit) {
        Set<Property> testProperties = RDFUtil.allPropertiesOf(r);
        for (Property p : omit) {
            testProperties.remove(p);
        }
        return testProperties;
    }

    private static boolean match(Resource expected, Resource actual, Property...props) {
        return buildTestModel(actual, props).isIsomorphicWith( buildTestModel(expected, props));
    }

    private static void testMatch(Resource expected, Resource actual, Property p) {
        Set<RDFNode> expectedValues = getValues(expected, p);
        Set<RDFNode> actualValues = getValues(actual, p);
        if (expectedValues.size() == 1 && expectedValues.iterator().next().isAnon()) {
            assertTrue("Must have value for wildcard property " + p, actualValues.size() > 0);
        } else {
            assertEquals("Compare property values for " + p, expectedValues, actualValues);
        }
    }

    private static Set<RDFNode> getValues(Resource expected, Property p) {
        return expected.listProperties(p).mapWith(new Map1<Statement,RDFNode>() {
            @Override public RDFNode map1(Statement s) { return s.getObject(); }
        }).toSet();
    }

    private static Model buildTestModel(Resource r, Property...props) {
        Model m = ModelFactory.createDefaultModel();
        Resource dest = r.inModel(m);
        for (Property p : props) {
            RDFUtil.copyProperty(r, dest, p);
        }
        return m;
    }

	public static Model modelFromTurtle(String ttl) {
		Model model = ModelFactory.createDefaultModel();
		return model.read( new StringReader(ttl), null, "Turtle");
	}

}

