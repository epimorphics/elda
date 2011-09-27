/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        Renderer.java
    Created by:  Dave Reynolds
    Created on:  2 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.renderers;

import com.epimorphics.lda.bindings.VarValues;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.util.MediaType;

/**
 * Abstraction for renderer
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
*/
public interface Renderer {

    /**
     	@return the mimetype which this renderer returns
     		in the given renderer context.
    */
    public MediaType getMediaType( VarValues rc );
    
    /**
     	Render a result set
    */
    public String render( VarValues rc, APIResultSet results );
}

