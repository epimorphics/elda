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
		
		System.err.println(">> mapLine(" + givenLine + ") " + " -- [" + blocks.size() + " blocks] ---------------------");

//		for (ShapeBlock sb: blocks) {
//			System.err.println(">>   " + sb);
//		}
		
		ShapeBlock prev = null;
		int i = 0;
		
		for (ShapeBlock sb: blocks) {
			
			System.err.println(">>   block " + ++ i + " " + sb);

			if (sb.firstLine > givenLine) {
				System.err.println(">> " + sb.firstLine + " > " + givenLine);
				System.err.println(">> prev = " + prev);
				int delta = givenLine - prev.firstLine;
				System.err.println(">>   delta = " + delta);
				
				return new Position(prev.filePath, prev.firstLine + delta);
			} else {
				prev = sb;
			}
		}		
		throw new RuntimeException("could not attain given line.");
	}
	
	@Override public int read(char[] cbuf, int offset, int limit) throws IOException {
		
		
		String content = layer.content;
		int contentPosition = layer.contentPosition;
		int nlPos = content.indexOf('\n', contentPosition);

		if (nlPos < 0) {
			
//			System.err.println(">> END OF " + layer.filePath);

			blocks.add(currentBlock);
			if (layers.isEmpty()) {	
				currentBlock = new ShapeBlock(lineCount, 0, layer.filePath);
				return -1; 
			} else {
				pop();
				currentBlock = new ShapeBlock(lineCount, 0, layer.filePath);
				return read(cbuf, offset, limit);
			}
		
		} else if (content.startsWith("#include ", contentPosition)) {
			
			String givenPath = content.substring(contentPosition + 9, nlPos);
						
			File sibling = new File(new File(layer.filePath).getParent(), givenPath);
			String fullPath = givenPath.startsWith("/") ? givenPath : sibling.toString(); 				

//			System.err.println(">> INCLUDE " + fullPath);
//			
//			System.err.println(">>   adding block " + currentBlock);
			
			blocks.add(currentBlock);
			currentBlock = new ShapeBlock(lineCount + 1, 0, fullPath);
//			System.err.println(">>   new block " + currentBlock);
			
			String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
			layer.contentPosition = nlPos + 1;
			push(fullPath, toInclude);
			return read(cbuf, offset, limit);
		
		} else {
			// TODO check that there's enough room for this line.
			lineCount += 1;
			currentBlock.linesCount += 1;
			
//			System.err.println(">> line " + lineCount + ": '" + content.substring(contentPosition, nlPos) + "'");
//			System.err.println(">>   " + currentBlock );
			
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