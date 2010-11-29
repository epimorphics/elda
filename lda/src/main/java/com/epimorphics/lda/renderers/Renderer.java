/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
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

import com.epimorphics.lda.core.APIResultSet;

/**
 * Abstraction for renderer
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public interface Renderer {

    /**
     * @return  the mimetype which this renderer returns
     * TODO should this be some class instead of a string?
     */
    public String getMimeType();
    
    /**
     * Render a result set
     */
    public Object render(APIResultSet results);
}

