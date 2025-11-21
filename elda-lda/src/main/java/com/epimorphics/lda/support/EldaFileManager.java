/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import org.apache.jena.util.FileManager;

public class EldaFileManager {

    protected static FileManager instance = FileManager.createStd();

    /**
     * Get the singleton FileManager instance.
     * @deprecated Use EldaStreamManager.get() instead.
     */
    @Deprecated()
    public static FileManager get() {
        return instance;
    }
}
