package com.epimorphics.lda.shortnames;

import java.util.Map;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;

public class ContextMaker {

	public static Context contextFrom( ShortnameService sns ) {
		Context result = new Context();
		NameMap nm = sns.nameMap();
	//
		Map<String, String> uriToNames = nm.stage2().result();
		for (Map.Entry<String, String> e: uriToNames.entrySet()) {
			result.recordPreferredName( e.getValue(), e.getKey() );
		}
	//
		// magic, like the magic in NameMap.
		result.recordPreferredName( "programming-language", DOAP.programming_language.getURI() );
		result.recordPreferredName( "bug-database", DOAP.bug_database.getURI() );
	//
		Map<String, ContextPropertyInfo> im = nm.getInfoMap();
		for (Map.Entry<String, ContextPropertyInfo> e: im.entrySet()) {
			result.setProperty( e.getKey(), e.getValue().clone() );
		}
		return result;
	}

}
