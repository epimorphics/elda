package com.epimorphics.lda.config.tests;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestIncludeReader {

	@Test public void testIncludeReader() {
		Model m = ModelFactory.createDefaultModel();
		Reader r = new ThingReader("includefiles/toplevel.ttl");
		m.read(r, "", "TTL");
		m.write(System.out, "TTL");
	}
	
	static class ThingReader extends Reader {
		
		final List<String> contents = new ArrayList<String>();
		final List<Integer> heres = new ArrayList<Integer>();
		final List<String> paths = new ArrayList<String>();
		
		int here = 0;
		String content;
		String filePath;
		
		public ThingReader(String fileSpec) {
			this.filePath = fileSpec;
			this.content = EldaFileManager.get().readWholeFileAsUTF8(fileSpec);
		}

		@Override public int read(char[] cbuf, int off, int len) throws IOException {
			int nlPos = content.indexOf('\n', here);
			if (nlPos < 0) {
				return pop();
			} else {
//				System.err.println(">> " + content.substring(here, nlPos));
				String line = content.substring(here, nlPos);
				if (line.startsWith("#include ")) {
					String foundPath = line.substring(9);
					File sibling = new File(new File(filePath).getParent(), foundPath);
					String fullPath = foundPath.startsWith("/") ? foundPath : sibling.toString(); 				
					String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
					push(toInclude);
				} else {
					for (int i = here; i < nlPos; i += 1) {
						cbuf[off++] = content.charAt(here);
					}				
				}
				
				int result = nlPos - here;
				here = nlPos + 1;
				return result;
			}
		}

		int count = 10;
		private int pop() {
			System.err.println(">> pop()");
			count -= 1;
			if (count == 0) throw new RuntimeException("DONEDONE");
			int which = contents.size() - 1;
			content = contents.remove(which);
			here = heres.remove(which);
			
			System.err.println(">> heres: " + heres);
			System.err.println(">> here " + here);
			System.err.println(">> length: " + content.length());
			
			int x = heres.isEmpty() && here >= content.length() ? -1 : 0;
			
			System.err.println(">> x: " + x);
			return x;
		}

		private void push(String toInclude) {
			contents.add(content);
			heres.add(here);
			paths.add(filePath);
		//
			content = toInclude;
			here = 0;
		}

		@Override public void close() throws IOException {
			
		}
		
	}
}
