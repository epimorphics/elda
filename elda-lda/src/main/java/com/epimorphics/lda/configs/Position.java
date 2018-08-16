package com.epimorphics.lda.configs;

import java.io.File;

/**
	A Position specifies a 1-origin line number and the
	pathname of the file the line number is in.
*/
public class Position {
	public final String pathName;
	public final int lineNumber;
	
	public Position(String pathName, int lineNumber) {
		this.pathName = pathName;
		this.lineNumber = lineNumber;
	}
	
	@Override public String toString() {
		return "<position " + lineNumber + " " + pathName + ">";
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Position && same((Position) other);
	}

	private boolean same(Position other) {
		return lineNumber == other.lineNumber && new File(pathName).equals(new File(other.pathName));
	}
}