/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.specs;

import java.util.*;

import com.epimorphics.lda.vocabularies.ELDA_API;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class MetadataOptions {

	/**
	    Returns an array of strings S which are the comma-separated
	    elements of all the elda:metadataOptions values of R, forced
	    to lower-case.
	*/
	public static String[] get( Resource R ) {
		List<String> result = new ArrayList<String>();
		List<Statement> options = R.listProperties( ELDA_API.metadataOptions ).toList();
    	if (options.size() > 0) 
    		for (Statement os: options)
    			for (String opt: os.getString().split( " *, *" ))
    				result.add( opt.toLowerCase() );
		return result.toArray( new String[result.size()]);
	}
}
