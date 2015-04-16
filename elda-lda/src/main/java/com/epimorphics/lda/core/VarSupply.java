/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.core;

import com.epimorphics.lda.rdfq.Variable;

/**
    Interface for any object (all right, an APIQuery) that can generate
    new variable names. Decouples ExpandLabels from APIQuery.
    
	@author chris
*/
public interface VarSupply {
	
	/**
	    Answer a new variable not equal to any that this VarSupply has
	    delivered before. 
	*/
	public Variable newVar();

}
