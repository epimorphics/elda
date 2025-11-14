/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.query;

import java.util.Set;

public interface WantsMetadata {

    public boolean wantsMetadata(String name);

    public Set<String> metaNames();

}
