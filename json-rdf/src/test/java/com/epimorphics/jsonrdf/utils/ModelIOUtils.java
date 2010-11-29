package com.epimorphics.jsonrdf.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringReader;

import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.vocabulary.XSD;

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
	    + "@prefix alt: <http://www.epimorphics.com/tools/exampleAlt#> .\n"
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
    
    public void modelToTurtleFile(Model m, String name) {
    	try {
			OutputStream os = new FileOutputStream( new File( name ) );
			m.write( os, "Turtle" );
			os.close();
		} catch (Exception e) {
			throw new JenaException(e);
		}
	}
}
