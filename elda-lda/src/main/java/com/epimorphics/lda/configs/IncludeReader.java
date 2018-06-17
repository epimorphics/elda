package com.epimorphics.lda.configs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.lda.support.EldaFileManager;

public class IncludeReader extends Reader {
	
	final List<Layer> layers = new ArrayList<Layer>();
	Layer layer = null;
	int lineCount = 0;

	public IncludeReader(String fileSpec) {
		this.layer = new Layer(EldaFileManager.get().readWholeFileAsUTF8(fileSpec), fileSpec);
	}

	public Position mapLine(int appendedLine) {
		return new Position("TBD", 0);
	}
	
	public static class Position {
		public final String pathName;
		public final int lineNumber;
		
		public Position(String pathName, int lineNumber) {
			this.pathName = pathName;
			this.lineNumber = lineNumber;
		}
	}

	@Override public int read(char[] cbuf, int offset, int limit) throws IOException {
		String content = layer.content;
		int contentPosition = layer.contentPosition;
		int nlPos = content.indexOf('\n', contentPosition);

		if (nlPos < 0) {
			
			if (layers.isEmpty()) {	
				return -1; 
			} else {
				pop();
				return read(cbuf, offset, limit);
			}
		
		} else if (content.startsWith("#include ", contentPosition)) {
			String givenPath = content.substring(contentPosition + 9, nlPos);
			
			File sibling = new File(new File(layer.filePath).getParent(), givenPath);
			String fullPath = givenPath.startsWith("/") ? givenPath : sibling.toString(); 				
			String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
			layer.contentPosition = nlPos + 1;
			push(fullPath, toInclude);
			return read(cbuf, offset, limit);
		
		} else {
			// TODO check that there's enough room for this line.
			
			lineCount += 1;
			// System.err.println(">> line " + lineCount + ": " + content.substring(contentPosition, nlPos));
			
			layer.content.getChars(contentPosition, nlPos + 1, cbuf, offset);
			int result = nlPos - contentPosition + 1;
			layer.contentPosition = nlPos + 1;
			return result;
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