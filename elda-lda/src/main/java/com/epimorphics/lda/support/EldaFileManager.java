/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import com.hp.hpl.jena.util.FileManager;

public class EldaFileManager {

	protected static FileManager instance = new FileManager();
	
	static { FileManager.setStdLocators( instance ); }
	
	public static FileManager get() {
		return instance;
	}
}
