package com.epimorphics.lda.config.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.RiotException;
import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.configs.IncludeReader;
import com.epimorphics.lda.configs.IncludeReader.Position;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestIncludeReader {
	
	@Test @Ignore public void testIncludeReaderTriple() throws IOException {
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
		System.err.println(">> OK.");
	}
	
	@Test public void testByExample() {
		IncludeReader r = new IncludeReader("includefiles/toplevel.ttl");
//		testByExample(r, 1, "includefiles/badtoplevel.ttl", 1);
//		testByExample(r, 2, "includefiles/badtoplevel.ttl", 2);
//		testByExample(r, 3, "includefiles/alpha.ttl", 1);
//		testByExample(r, 4, "includefiles/alpha.ttl", 2);
//		testByExample(r, 5, "includefiles/alpha.ttl", 3);
//		testByExample(r, 6, "includefiles/alpha.ttl", 4);
//		testByExample(r, 7, "includefiles/alpha.ttl", 5);
//		testByExample(r, 8, "includefiles/badtoplevel.ttl", 4);
//		testByExample(r, 9, "includefiles/badbeta.ttl", 1);
//		testByExample(r, 10, "includefiles/badbeta.ttl", 2);
//		testByExample(r, 11, "includefiles/badbeta.ttl", 3);
//		testByExample(r, 12, "includefiles/badbeta.ttl", 4);
//		testByExample(r, 13, "includefiles/badbeta.ttl", 5);
//		testByExample(r, 14, "includefiles/badtoplevel.ttl", 6);
	}

	private void testByExample(IncludeReader r, int givenLine, String expectPath, int expectLine) {
		Position where = r.mapLine(givenLine);
		assertEquals("path name is wrong", expectPath, where.pathName);
		assertEquals("line number is wrong", expectLine, where.lineNumber);
	}

	@Test @Ignore public void testIncludeReaderException() throws IOException {
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
			assertEquals("wrong line number", 3, where.lineNumber);
			assertEquals("wrong path", "", where.pathName);
		}
		r.close();
	}
}
