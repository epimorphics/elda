/*
	See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
	(c) Copyright 2011 Epimorphics Limited
	$Id$
	
	File:        APIQuery.java
	Created by:  Dave Reynolds
	Created on:  31 Jan 2010
*/
package com.epimorphics.lda.tests_support;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.EldaException;
import com.hp.hpl.jena.rdf.model.Model;

/**
	A ModelLoadI that throws a NotFoundException whatever uri is
	passed to loadModel. 
 
	@author chris
*/
public class LoadsNothing implements ModelLoader
	{
	public static final LoadsNothing instance = new LoadsNothing();
	
	@Override public Model loadModel( String uri) 
		{ EldaException.NotFound( "model", uri ); return /* never */ null; }
	}