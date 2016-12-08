/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.jsonrdf;

import java.io.Reader;
import java.io.StringReader;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.io.JSONMaker;
import org.apache.jena.atlas.json.io.parser.JSONParser;

public class ParseWrapper {

	public static JsonObject stringToJsonObject(String s) {
		JSONMaker jm = new JSONMaker();
		JSONParser.parseAny(new StringReader(s), jm);
		return jm.jsonValue().getAsObject();
	}

	public static JsonArray stringToJsonArray(String s) {
		JSONMaker jm = new JSONMaker();
		JSONParser.parseAny(new StringReader(s), jm);
		return jm.jsonValue().getAsArray();
	}
	
	public static JsonObject readerToJsonObject(Reader r) {
		JSONMaker jm = new JSONMaker();
		JSONParser.parseAny(r, jm);
		return jm.jsonValue().getAsObject();
	}

}
