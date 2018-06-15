package com.epimorphics.lda.config.tests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.RiotException;
import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.configs.IncludeReader;
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
		System.err.println(">> OK.");
	}

	@Test public void testIncludeReaderException() throws IOException {
		Model m = ModelFactory.createDefaultModel();
		IncludeReader r = new IncludeReader("includefiles/badtoplevel.ttl");
		try {
			m.read(r, "", "TTL");
			
			System.err.println(">> resolving: " + r.mapLine(11));
			
		} catch (RiotException re) {
			String message = re.getMessage();
			System.err.println(">> " + message);
			Pattern p = Pattern.compile("line: ([0-9]+)");
			Matcher mat = p.matcher(message);
			mat.find();
			int intLine = Integer.parseInt(mat.group(1));
			System.err.println();
			System.err.println("@ line " + intLine);
			System.err.println(">> actual line: " + r.mapLine(intLine));
		}
		r.close();
	//
		// fail("exception not caught");
	}
}
