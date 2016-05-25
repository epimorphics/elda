package com.epimorphics.lda.licence;

import java.util.*;

import com.epimorphics.lda.exceptions.UnknownShortnameException;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;

public class Extractor {

	final APIEndpointSpec spec;
	
	public Extractor(APIEndpointSpec spec) {
		this.spec = spec;
	}

	public Set<Resource> getLicenceResources(List<Resource> items) {
		Set<Resource> result = new HashSet<Resource>();
		String licenceQuery = constructLicenceQuery(result, items);
		result.addAll(runLicenceQuery(licenceQuery));		
		return result;
	}

	public String constructLicenceQuery(Set<Resource> plainLicences, List<Resource> items) {
		
		Set<String> paths = new HashSet<String>();
		
		for (RDFNode l: spec.getLicenceNodes()) {			
			if (l.isResource()) 
				plainLicences.add(l.asResource());
			else
				paths.add(l.asLiteral().getLexicalForm());
		}		
		
		if (paths.size() > 0) {
			List<String> queryLines = new ArrayList<String>();
			queryLines.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
			queryLines.add("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
			queryLines.add("PREFIX elda: <http://www.epimorphics.com/vocabularies/lda#>");
		//
			queryLines.add("CONSTRUCT { ?license a elda:licence ; ?p ?v } WHERE {");
		//
			queryLines.add("VALUES ?item {");
			for (Resource item: items) queryLines.add("<" + item.getURI() + ">");
			queryLines.add("  }");
		//
			queryLines.add("  {");
			addPaths(paths, queryLines);
			queryLines.add("  }");
		//
			queryLines.add("}");
			return org.apache.commons.lang.StringUtils.join(queryLines, "\n");
		}
		return null;
	}

	public void addPaths(Set<String> paths, List<String> queryLines) {
		ShortnameService sns = spec.getAPISpec().getShortnameService();
		
		boolean first = true;
		String currentVar = "item";
		String prefix = "";
		
		for (String path: paths) {
			if (!first) queryLines.add("    UNION");
			first = false;
			String prev = "";
			// handle a path
			String [] elements = path.split("\\.");
			int remaining = elements.length;
			for (String element: elements) {
				remaining -= 1;
				boolean last = remaining == 0;
				String newPrev = prev + "_" + predicateName(sns, element);
				String nextVar = (last ? "license" : newPrev);
				prev = newPrev;
				String S = "?" + currentVar;
				String P = "<" + predicateURI(sns, element) + ">";
				String O = "?" + nextVar;
				if (isInverse(element)) {
					queryLines.add("  " + O + " " + P + " " + S + " .");
				} else {
					queryLines.add("  " + S + " " + P + " " + O + " .");
				}
				prefix = prefix + "_" + nextVar;
				currentVar = nextVar;
			}
			queryLines.add("  OPTIONAL { ?license ?p ?v } ");
		}
	}
	
	public Set<Resource> runLicenceQuery(String licenceQuery) {
	    Source dataSource = spec.getAPISpec().getDataSource();
		final Set<Resource> licences = new HashSet<Resource>();
		if (licenceQuery != null) {
			Model cons = dataSource.executeConstruct(QueryFactory.create(licenceQuery));
			licences.addAll(cons.listSubjects().toSet());
		}
		return licences;
	}
    
    private String predicateURI(ShortnameService sns, String element) {
    	if (element.startsWith("~")) element = element.substring(1);
    	String URI = sns.expand(element);
    	if (URI == null) throw new UnknownShortnameException(element);
		return URI;
	}
    
    private String predicateName(ShortnameService sns, String element) {
    	if (element.startsWith("~")) element = element.substring(1);
    	return element;
	}
    
    private boolean isInverse(String element) {
    	return element.startsWith("~");
    }
}
