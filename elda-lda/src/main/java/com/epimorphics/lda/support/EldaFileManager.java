package com.epimorphics.lda.support;

import com.hp.hpl.jena.util.FileManager;

public class EldaFileManager {

	protected static FileManager instance = new FileManager();
	
	static { FileManager.setStdLocators( instance ); }
	
	public static FileManager get() {
		return instance;
	}
}
