/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.tests;

import com.epimorphics.lda.core.NamedViews;
import com.epimorphics.lda.core.View;

public final class FakeNamedViews implements NamedViews 
	{
	final View v = new View();
	
	@Override public View getView(String viewname) { return v; }
	
	@Override public View getDefaultView() { return v; }
	}