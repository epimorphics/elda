package com.epimorphics.lda.config.tests;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestIncludeReader {

	@Test @Ignore public void testIncludeReader() throws IOException {
		Model m = ModelFactory.createDefaultModel();
		Reader r = new ThingReader("includefiles/toplevel.ttl");
		
		boolean runRiot = true;
		
		if (runRiot) {
			try {
				m.read(r, "", "TTL");
			} catch (Throwable e) {
				e.printStackTrace(System.err);
				throw e;
			}
			System.err.println(">> ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;" );
			m.write(System.out, "TTL");
			
		} else {
			
			while (true) {
				char [] cbuf = new char[20000];
				int n = r.read(cbuf, 0, cbuf.length);
				if (n < 0) break;
				for (int i = 0; i < n; i += 1)
					System.err.print(cbuf[i]);
				}
			}
	}
	
	static class ThingReader extends Reader {
		
		final List<String> contents = new ArrayList<String>();
		final List<Integer> heres = new ArrayList<Integer>();
		final List<String> paths = new ArrayList<String>();
		
		int here = 0;
		String content;
		String filePath;
		
		int lineCount = 0;
		
		public ThingReader(String fileSpec) {
			this.filePath = fileSpec;
			this.content = EldaFileManager.get().readWholeFileAsUTF8(fileSpec);
		}

		@Override public int read(char[] cbuf, int off, int len) throws IOException {
			int nlPos = content.indexOf('\n', here);
			if (nlPos < 0) {
				return pop();
			} else {
				String subs = content.substring(here, nlPos);
				String line = subs;
				if (line.startsWith("#include ")) {
					String foundPath = line.substring(9);
					File sibling = new File(new File(filePath).getParent(), foundPath);
					String fullPath = foundPath.startsWith("/") ? foundPath : sibling.toString(); 				
					String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
					here = nlPos + 1;
					push(fullPath, toInclude);
					return read(cbuf, off, len);
				} else {
					lineCount += 1;
					
					for (int i = here; i < nlPos; i += 1) {
						cbuf[off++] = content.charAt(i);
					}			
					cbuf[off++] = '\n';
					
					int result = nlPos - here + 1;
					here = nlPos + 1;
					return result;
				}
			}
		}
		
		private int pop() {
			
			if (heres.isEmpty()) {
				return -1;
			}
			
			int which = contents.isEmpty() ? 0 : contents.size() - 1;
			
			content = contents.remove(which);
			here = heres.remove(which);
			filePath = paths.remove(which);
						
			int returnCode = heres.isEmpty() && here >= content.length() ? -1 : 0;
			return returnCode;
		}

		private void push(String filePath, String toInclude) {
			contents.add(content);
			heres.add(here);
			paths.add(filePath);
		//
			this.content = toInclude;
			this.here = 0;
			this.filePath = filePath;
		}

		@Override public void close() throws IOException {
			// nothing to do.
		}
		
	}
}
