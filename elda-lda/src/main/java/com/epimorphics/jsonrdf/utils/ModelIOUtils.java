/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.jsonrdf.utils;

import java.io.*;

import com.epimorphics.lda.vocabularies.API;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.XSD;

public class ModelIOUtils {

	/**
	    The prefixes used by default by modelFromTurtle.
	*/
	public static final String PREFIXES = 
	    "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"
	    + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
	    + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
	    + "@prefix xsd: <" + XSD.getURI() + "> .\n"
	    + "@prefix api: <" + API.getURI() + "> .\n"
	    + "@prefix foaf: <" + FOAF.getURI() + "> .\n"
	    + "@prefix alt: <http://www.epimorphics.com/tools/exampleAlt#> .\n"
	    + "@prefix elda: <" + "http://www.epimorphics.com/vocabularies/lda#" + "> .\n"
	    + "@prefix : <http://www.epimorphics.com/tools/example#> .\n";

	/**
	    Create a model by reading the Turtle string ttl, using the prefixes
	    from PREFIXES.
	*/
	public static Model modelFromTurtle(String ttl) {
		Model model = ModelFactory.createDefaultModel();
		model.read( new StringReader(ModelIOUtils.PREFIXES + ttl), null, "Turtle");
		return model;
	}
    
	/**
	    Write the model <code>m</code> to the file named <code>fileName</code>
	    in Turtle format.
	*/
    public void modelToTurtleFile(Model m, String fileName) {
    	try {
			OutputStream os = new FileOutputStream( new File( fileName ) );
			m.write( os, "Turtle" );
			os.close();
		} catch (Exception e) {
			throw new JenaException(e);
		}
	}
    
    /**
        Answer a serialisation of <code>m</code> in the format 
        <code>format</code>.
    */
    public static String renderModelAs( Model m, String format ) {
        StringWriter sw = new StringWriter();
        m.write( sw, format );
        return sw.toString();
    }
}
