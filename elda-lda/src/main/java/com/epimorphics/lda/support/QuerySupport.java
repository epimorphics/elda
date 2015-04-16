/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
 */

package com.epimorphics.lda.support;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.query.APIQuery;
import com.epimorphics.lda.rdfq.*;
import com.epimorphics.lda.rdfq.RDFQ.Triple;
import com.epimorphics.lda.textsearch.TextSearchConfig;

public class QuerySupport {

	static Logger log = LoggerFactory.getLogger(APIQuery.class);

	private static boolean promoteAnySubject = true;

	public static final Any text_query = RDFQ.uri(TextSearchConfig.JENA_TEXT_QUERY.getURI());
	
	/**
	    <p>
	    	Reorder the given triples to try and arrange that query engines with weak
	 		optimisers aren't given excessively silly queries. So rdf:type statements
	 		(which are usually less useful than specific properties) are moved down
	 		the order, and triples with literal objects (which often only appear a
	 		few times) are moved up. All optional triples are moved to the end
	 		regardless of their structure.
	 	</p>
	 	
	 	<p>
	 		Uses of the text query property are pushed early if tqFirst
	 		is true, and later (just before the types) if it is false. 
	 	</p>
	 
	 	@param triples the list of triples to re-order.
	 	@return a fresh list of triples, a reordered version of triples.
	*/
	public static List<Triple> reorder(List<Triple> triples, boolean tqFirst) {
		List<Triple> plain = new ArrayList<Triple>(triples.size());
		List<Triple> hasLiteral = new ArrayList<Triple>(triples.size());
		List<Triple> typed = new ArrayList<Triple>(triples.size());
		List<Triple> lateTextQueries = new ArrayList<Triple>(triples.size());
		for (Triple t : triples) {
			if (t.P.equals(text_query))
				(tqFirst ? hasLiteral : lateTextQueries).add(t);
			else if (t.O instanceof Value && canPromoteSubject(t.S))
				hasLiteral.add(t);
			else if (t.P.equals(RDFQ.RDF_TYPE))
				typed.add(t);
			else
				plain.add(t);
		}
	//
		List<Triple> result = new ArrayList<Triple>(triples.size());
		result.addAll(hasLiteral);
		result.addAll(plain);
		result.addAll(lateTextQueries);
		result.addAll(typed);
		if (!result.equals(triples))
			log.debug("reordered\n    " + triples + "\nto\n    " + result);
		return result;
	}

	public static boolean canPromoteSubject(Any S) {
		return promoteAnySubject || S.equals(APIQuery.SELECT_VAR);
	}
}

/*
 * (c) Copyright 2010 Epimorphics Limited All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
