package com.epimorphics.lda.licence.tests;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.licence.Extractor;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestPathConstruction {

	final Model model = ModelFactory.createDefaultModel();
	
	final Resource root = model.createResource("eh:/the-spec");
	
	final Resource A = model.createResource("eh:/A");
	final Resource B = model.createResource("eh:/B");
	final Resource C = model.createResource("eh:/C");

	final Resource sparql = model.createResource("eh:/sparqlEndpoint");
	
	final Resource theEndpoint = model.createResource("eh:/the-endpoint");
	
	static final ModelLoader loader = new ModelLoader() {

		@Override public Model loadModel(String uri) {
			return null;
		}
		
	};
	
	final String [][] them = new String[] [] {
		new String[] {"A", "?item <eh:/A> ?license ."}
		
		, new String[] {"~A", "?license <eh:/A> ?item ."}
		
		, new String[] {"A.B", "?item <eh:/A> ?_A .", "?_A <eh:/B> ?license ."}
		
		, new String[] {"~A.B", "?_A <eh:/A> ?item .", "?_A <eh:/B> ?license ."}
		
		, new String[] {"A.~B", "?item <eh:/A> ?_A .", "?license <eh:/B> ?_A ."}
		
		, new String[] {"A.B.C", 
			"?item <eh:/A> ?_A ."
			, "?_A <eh:/B> ?_A_B ."
			, "?_A_B <eh:/C> ?license ."
		}
		, new String[] {"A.B", "?item <eh:/A> ?_A .", "?_A <eh:/B> ?license ."}
	};	
	
	@Test public void ByParameters() {
		setBaseConfig();		
		APISpec spec = new APISpec(FileManager.get(), root, loader);
		
		for (String [] instance: them ) {
			Extractor e = new Extractor(spec.getEndpoints().get(0));
			List<String> queryLines = new ArrayList<String>();
			
			e.addPaths(one(instance[0]), queryLines);
					
			assertEquals(instance.length, queryLines.size());
			for (int i = 1; i < instance.length; i += 1) {
				String expected = instance[i];
//				System.err.println(">> expecting [" + i + "]: " + expected);
				assertEq(expected, queryLines.get(i-1));
			}
		}
	}
	
	// Equality counting any run of spaces as one space and no 
	// leading/trailing spaces.
	private void assertEq(String expected, String obtained) {
		assertEquals(expected, obtained.trim().replaceAll(" +", " "));
	}

	private Set<String> one(String string) {
		Set<String> result = new HashSet<String>();
		result.add(string);
		return result;
	}

	private void setBaseConfig() {
		model.add(root, RDF.type, API.API);
		model.add(root, API.sparqlEndpoint, sparql);
		model.add(root, API.endpoint, theEndpoint);
		
		model.add(theEndpoint, RDF.type, API.ListEndpoint);
		model.add(theEndpoint, API.uriTemplate, "/an/endpoint");
		
		model.add(A, RDF.type, RDF.Property);
		model.add(A, API.label, "A");
		
		model.add(B, RDF.type, RDF.Property);
		model.add(B, API.label, "B");
		
		model.add(C, RDF.type, RDF.Property);
		model.add(C, API.label, "C");
	}
}
