package com.epimorphics.lda.configs;

/**
	A ShapeBlock is a description of a block of included text.
	firstLine is the line number in the included stream of
	this block, linesCount is the size of the block, and filePath
	is the name of the file that the 

*/
public class ShapeBlock {
	int firstLine;
	int linesCount;
	String filePath;
	
	public ShapeBlock(int firstLine, int linesCount, String filePath) {
		this.firstLine = firstLine;
		this.linesCount = linesCount;
		this.filePath = filePath;
	}
}