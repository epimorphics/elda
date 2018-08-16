package com.epimorphics.lda.config.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.RiotException;
import org.junit.Test;

import com.epimorphics.lda.configs.IncludeReader;
import com.epimorphics.lda.configs.Position;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestIncludeReader {
	
	@Test public void testIncludeReaderTriple() throws IOException {
		Model m = ModelFactory.createDefaultModel();
		Reader r = new IncludeReader("includefiles/toplevel.ttl");
		m.read(r, "", "TTL");
		r.close();
	//
		Model expect = ModelFactory.createDefaultModel();
		Resource S = expect.createResource(ELDA_API.NS + "example");
		Property P = RDF.type;
		RDFNode O = XSD.xstring;
		expect.add(S, P, O);
	//
		if (!m.isIsomorphicWith(expect)) fail("did not read concatenated turtle.");
		// System.err.println(">> OK.");
	}
	
	@Test public void testByExample1() {
		testByExample(1, "includefiles/toplevel.ttl", 1);
	}
	
	@Test public void testByExample2() {
		testByExample(2, "includefiles/toplevel.ttl", 2);
	}

	@Test public void testByExample3() {
		testByExample(3, "includefiles/alpha.ttl", 1);
	}

	@Test public void testByExample4() {
		testByExample(4, "includefiles/alpha.ttl", 2);
	}

	@Test public void testByExample5() {
		testByExample(5, "includefiles/alpha.ttl", 3);
	}

	@Test public void testByExample6() {
		testByExample(6, "includefiles/alpha.ttl", 4);
	}

	@Test public void testByExample7() {
		testByExample(7, "includefiles/alpha.ttl", 5);
	}

	@Test public void testByExample8() {
		testByExample(8, "includefiles/toplevel.ttl", 4);
	}

	@Test public void testByExample9() {
		testByExample(9, "includefiles/beta.ttl", 1);
	}

	@Test public void testByExample10() {
		testByExample(10, "includefiles/beta.ttl", 2);
	}

	@Test public void testByExample11() {
		testByExample(11, "includefiles/beta.ttl", 3);
	}

	@Test public void testByExample12() {
		testByExample(12, "includefiles/beta.ttl", 4);
	}

	@Test public void testByExample13() {
		testByExample(13, "includefiles/beta.ttl", 5);
	}

	@Test public void testByExample14() {
		testByExample(14, "includefiles/toplevel.ttl", 6);
	}

	static final IncludeReader r = new IncludeReader("includefiles/toplevel.ttl");
	
	static final Model m = ModelFactory.createDefaultModel();
	
	static { m.read(r, "", "TTL"); }

	private void testByExample(int givenLine, String expectPath, int expectLine) {
		expectPath = expectPath.replaceAll("/", File.separator);
		Position expect = new Position(expectPath, expectLine);
	//
		Position where = r.mapLine(givenLine);
		assertEquals("wrong position for " + givenLine, expect, where);
		assertEquals(expect.pathName, where.pathName);
	}

	@Test public void testIncludeReaderException() throws IOException {
		Model m = ModelFactory.createDefaultModel();
		IncludeReader r = new IncludeReader("includefiles/badtoplevel.ttl");
		try {
			m.read(r, "", "TTL");	
			fail("should have caught bad beta");
		} catch (RiotException re) {
			
			String message = re.getMessage();
			Pattern p = Pattern.compile("line: ([0-9]+)");
			Matcher mat = p.matcher(message);
			assertTrue("did not find line: element in message", mat.find());
			int intLine = Integer.parseInt(mat.group(1));
			Position where = r.mapLine(intLine);
			assertEquals("wrong line number", 8, where.lineNumber);
			assertEquals("wrong path", "includefiles/badtoplevel.ttl", where.pathName);
		}
		r.close();
	}
}
