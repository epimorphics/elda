package com.epimorphics.lda.configs;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.epimorphics.lda.support.EldaFileManager;

/**
	An IncludeReader pulls content from a file and
	replaces lines of the form "#include fileName"
	with (recursively) the contents of the named
	file.
*/
public class IncludeReader extends Reader {
	
	/**
		The line start indicating an include command.
	*/
	public static final String INCLUDE = "#include ";
	
	final List<Layer> layers = new ArrayList<Layer>();
	Layer layer = null;
	int lineCount = 0;
	ShapeBlock currentBlock; 
	List<ShapeBlock> blocks = new ArrayList<ShapeBlock>();
	
	/**
		Initialise this IncludeReader to read from the file named
		by fileSpec.
	*/
	public IncludeReader(String fileSpec) {
		this.layer = new Layer(EldaFileManager.get().readWholeFileAsUTF8(fileSpec), fileSpec);
		this.currentBlock = new ShapeBlock(1, 0, fileSpec);
	}

	/**
		Given a line number relative to the complete included
		text, mapLine returns a position giving a line number
		relative to a named included file.
	*/
	public Position mapLine(int givenLine) {
//		System.err.println();
//		System.err.println(">> mapLine(" + givenLine + ") " + " -- [" + blocks.size() + " blocks] ---------------------");
		
		ShapeBlock prev = null;
//		int i = 0;
		
		for (ShapeBlock sb: blocks) {
			
//			 System.err.println(">>   block " + ++ i + " " + sb);

			if (sb.firstLine > givenLine) {
				int base = prev.linesCount;
				int delta = givenLine - prev.firstLine;				
				return new Position(prev.filePath, base + delta + 1 );
			
			} else {
				prev = sb;
			}
		}
		ShapeBlock last = blocks.get(blocks.size() - 1);
		return new Position(last.filePath, givenLine - last.firstLine + last.linesCount + 2);
	}
	
	/**
		Obey the Reader.line specification, also tracking the global line
		number, the stack of layers holding sleeping state, and the
		sequence of fragment descriptions allowing mapping back from
		global line number to file-and-line-number.
	*/
	@Override public int read(char[] cbuf, int offset, int limit) throws IOException {		
		String content = layer.content;
		int contentPosition = layer.contentPosition;
		int nlPos = content.indexOf('\n', contentPosition);

		if (nlPos < 0) {
			blocks.add(currentBlock);
			if (layers.isEmpty()) {	
				return -1; 
			} else {
				pop();
				currentBlock = new ShapeBlock(lineCount + 1, layer.lineCount + 1, layer.filePath);
				return read(cbuf, offset, limit);
			}
		
		} else if (content.startsWith(INCLUDE, contentPosition)) {
			
			String givenPath = content.substring(contentPosition + INCLUDE.length(), nlPos).trim();
						
			File sibling = new File(new File(layer.filePath).getParent(), givenPath);
			String fullPath = givenPath.startsWith("/") ? givenPath : sibling.toString(); 				
						
			blocks.add(currentBlock);
			currentBlock = new ShapeBlock(lineCount + 1, 0, fullPath);
			
			String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
			layer.contentPosition = nlPos + 1;
			push(fullPath, toInclude);
			return read(cbuf, offset, limit);
		
		} else {
			// TODO check that there's enough room for this line.
			lineCount += 1;
			layer.lineCount += 1;
			// currentBlock.linesCount += 1;
						
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
		if (someLayerContainsPath(filePath)) {
			throw new RuntimeException("recursive use of " + filePath + " at line: " + lineCount);
		}
		layer = new Layer(toInclude, filePath);
	}

	private boolean someLayerContainsPath(String filePath) {
		for (Layer l: layers) {
			if (l.filePath.equals(filePath)) return true;
		}
		return false;
	}

	@Override public void close() throws IOException {
	}
}