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
	
	final List<ShapeBlock> shapeBlocks = new ArrayList<ShapeBlock>();
	
	Layer layer = new Layer("", "");
	
	ShapeBlock currentShape = null;
	
	int lastBlockStart = 0;
	
	int lineCount = 1;
	
	final Map<String, String> seen = new HashMap<String, String>();
	
	final Map<String, Integer> sizes = new HashMap<String, Integer>();
	
	public IncludeReader(String fileSpec) {
		this.layer = new Layer(EldaFileManager.get().readWholeFileAsUTF8(fileSpec), fileSpec);
		this.currentShape = new ShapeBlock(0, 1, fileSpec);
	}

	public String mapLine(int appendedLine) {
		
		System.err.println(">> mapLine(" + appendedLine + ")");
		for (ShapeBlock sb: shapeBlocks) {
			System.err.println(">> fragment @" + sb.firstLine + " size: " + sb.linesCount + " file:" + sb.filePath);
		}
		
		int n = 0;
		ShapeBlock prev = null;
		for (ShapeBlock sb: shapeBlocks) {
			n += 1;
			System.err.println(">> sb " + n);
			if (appendedLine > sb.firstLine) {
				System.err.println(">>  next");
				prev = sb;
			} else {
				System.err.println(">>  go");
				int delta = appendedLine - sb.firstLine;
				return "GO: " + delta;
			}
		}
		System.err.println(">> SIZE: " + prev.filePath + " is " + sizes.get(prev.filePath));
		return "FIN: " + (appendedLine - prev.firstLine) + " " + prev.filePath;
	}

	@Override public int read(char[] cbuf, int off, int len) throws IOException {
		String content = layer.content;
		int contentPosition = layer.contentPosition;
		int nlPos = content.indexOf('\n', contentPosition);
	//
		if (nlPos < 0) {
			
			if (layers.isEmpty()) {
				sizes.put(layer.filePath, layer.lineCount);
				appendBlock(lastBlockStart,layer.filePath);
				return -1;
			}
			pop();
			appendBlock(lastBlockStart, layer.filePath);
			lastBlockStart = lineCount;
			return read(cbuf, off, len);
		
		} else if (content.startsWith("#include ", contentPosition)) {
			String givenPath = content.substring(contentPosition + 9, nlPos);
			
			// currentShape.linesCount += 1;
			appendBlock(lastBlockStart, givenPath);
			lastBlockStart = lineCount;
			
			File sibling = new File(new File(layer.filePath).getParent(), givenPath);
			String fullPath = givenPath.startsWith("/") ? givenPath : sibling.toString(); 				
			String toInclude = EldaFileManager.get().readWholeFileAsUTF8(fullPath);
			layer.contentPosition = nlPos + 1;
			push(fullPath, toInclude);
			return read(cbuf, off, len);
		} else {
			layer.content.getChars(contentPosition, nlPos + 1, cbuf, off);
			
			lineCount += 1;
			layer.lineCount += 1;
			currentShape.linesCount += 1;
			
			int result = nlPos - contentPosition + 1;
			layer.contentPosition = nlPos + 1;
			return result;
		}
	}
	
	void appendBlock(int lastBlockStart, String foundPath) {
		shapeBlocks.add(currentShape);
		currentShape = new ShapeBlock(lastBlockStart, 0, foundPath);
	}
	
	private void pop() {
		int which = layers.size() - 1;
		sizes.put(layer.filePath, layer.lineCount);
		layer = layers.remove(which);
	}

	private void push(String filePath, String toInclude) {
		layers.add(layer);
		layer = new Layer(toInclude, filePath);
	}

	@Override public void close() throws IOException {
	}
	
	
	
	
}