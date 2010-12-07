/*
	See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
	(c) copyright Epimorphics Limited 2010
	$Id$
	
	File:        APIQuery.java
	Created by:  Dave Reynolds
	Created on:  31 Jan 2010
*/
package com.epimorphics.lda.tests_support;

import com.epimorphics.lda.core.ModelLoaderI;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.NotFoundException;

/**
	A ModelLoadI that throws a NotFoundException whatever uri is
	passed to loadModel. 
 
	@author chris
*/
public class LoadsNothing implements ModelLoaderI
	{
	@Override public Model loadModel(String uri) 
		{
		throw new NotFoundException( uri );
		}
	}