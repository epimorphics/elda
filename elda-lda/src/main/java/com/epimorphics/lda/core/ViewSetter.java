/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.core;

/**
    A ViewSetter chooses a view based on name or properties,
    and can also set formatting.
    
 	@author chris
*/
public interface ViewSetter {
	public void setViewByName( String viewName );
	public void setViewByProperties(String val);
	public void setFormat(String val);
}