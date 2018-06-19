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
	ShapeBlock currentBlock; 
	List<ShapeBlock> blocks = new ArrayList<ShapeBlock>();
	
	public IncludeReader(String fileSpec) {
		this.layer = new Layer(EldaFileManager.get().readWholeFileAsUTF8(fileSpec), fileSpec);
		currentBlock = new ShapeBlock(1, 0, fileSpec);
	}

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
		
		} else if (content.startsWith("#include ", contentPosition)) {
			
			String givenPath = content.substring(contentPosition + 9, nlPos);
						
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
		layer = new Layer(toInclude, filePath);
	}

	@Override public void close() throws IOException {
	}
}