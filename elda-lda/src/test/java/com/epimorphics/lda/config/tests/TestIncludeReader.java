package com.epimorphics.lda.config.tests;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.RiotException;
import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.support.EldaFileManager;
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
		Reader r = new ThingReader("includefiles/toplevel.ttl");
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
	}

	@Test public void testIncludeReaderException() throws IOException {
		Model m = ModelFactory.createDefaultModel();
		ThingReader r = new ThingReader("includefiles/badtoplevel.ttl");
		try {
			m.read(r, "", "TTL");
		} catch (RiotException re) {
			String message = re.getMessage();
			System.err.println(">> " + message);
			Pattern p = Pattern.compile("line: ([0-9]+)");
			Matcher mat = p.matcher(message);
			mat.find();
			int intLine = Integer.parseInt(mat.group(1));
			System.err.println("@ line " + intLine);
			System.err.println(">> actual line: " + r.mapLine(intLine));
		}
		r.close();
	//
		fail("exception not caught");
	}
	
	static class ThingReader extends Reader {
		
		final List<Layer> layers = new ArrayList<Layer>();
		
		Layer layer = new Layer("", "");
		
		int lineCount = 0;
		
		final Map<String, String> seen = new HashMap<String, String>();
		
		public String mapLine(int appendedLine) {
			return "TODO";
		}
		
		public ThingReader(String fileSpec) {
			this.layer = new Layer(EldaFileManager.get().readWholeFileAsUTF8(fileSpec), fileSpec);
		}

		@Override public int read(char[] cbuf, int off, int len) throws IOException {
			String content = layer.content;
		//
			int nlPos = content.indexOf('\n', layer.here);
			if (nlPos < 0) {
				if (layers.isEmpty()) return -1;
				pop();
				return read(cbuf, off, len);
			} else {
				String subs = content.substring(layer.here, nlPos);
				String line = subs;
								
				if (line.startsWith("#include ")) {
					String foundPath = line.substring(9);
					File sibling = new File(new File(layer.filePath).getParent(), foundPath);
					String fullPath = foundPath.startsWith("/") ? foundPath : sibling.toString(); 				
					String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
					layer.here = nlPos + 1;
					push(fullPath, toInclude);
					return read(cbuf, off, len);
				} else {
					lineCount += 1;
										
					for (int i = layer.here; i < nlPos; i += 1) {
						cbuf[off++] = layer.content.charAt(i);
					}			
					cbuf[off++] = '\n';
					
					int result = nlPos - layer.here + 1;
					layer.here = nlPos + 1;
					return result;
				}
			}
		}
		
		public static class Layer {
			String content;
			int here;
			String filePath;
			
			Layer(String content, String filePath) {
				this.content = content;
				this.here = 0;
				this.filePath = filePath;
			}
		}
		
		private void pop() {
			int which = layers.size() - 1;
			layer = layers.remove(which);
		}

		private void push(String filePath, String toInclude) {
			layers.add(layer);
			layer = new Layer(toInclude, filePath);
		}

		@Override public void close() throws IOException {
		}
		
	}
}
