/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.jsonrdf.utils;

import com.hp.hpl.jena.rdf.model.Model;

public class ModelCompareUtils {

	/**
	 	Compare <code>expected</code> and <code>obtained</code> for isomorphism. 
	 	If they are not isomorphic, print out their shared submodel and the two 
	 	different parts. Answer whether or not they're isomorphic. 
	*/
	public static boolean compareAndDisplayDifferences( Model expected, Model obtained ) {
		boolean isIso = expected.isIsomorphicWith( obtained );
		if (!isIso)
			{
			Model shared = expected.intersection(obtained);
			System.err.println( ">> shared:" );
			shared.write( System.err, "Turtle" );
			System.err.println( ">> expected [-shared]: " );
			expected.difference(shared).write(System.err, "Turtle" );
			System.err.println( ">> computed [-shared]: " );
			obtained.difference(shared).write(System.err, "Turtle" );
			}
		return isIso;
	}

}
