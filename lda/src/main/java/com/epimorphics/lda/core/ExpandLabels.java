/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.core;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.lda.sources.Source;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Contains the code to expand labels.
 
 	@author chris
*/
public class ExpandLabels {

	private final VarSupply vars;
	
	public ExpandLabels( VarSupply vars ) { 
		this.vars = vars; 
	}
	
	/**
	 	Adds to the ResultSet the labels that can be found by consulting the Source.
	 	The VarSupply is used to create new, independant variables.
	*/
	void expand( Source source, APIResultSet results) {
	    Set<Resource> varAllocation = buildVarAllocation(results);
	    if (varAllocation.isEmpty()) return;
	    String labelQuery = buildLabelQuery(varAllocation);
	    // runLabelQuery(source, results, varAllocation, labelQuery);                    
	}

	private Set<Resource> buildVarAllocation(APIResultSet results) {
		Set<Resource> varAllocation = new HashSet<Resource>();
	    for (Resource root : results.getResultList()) {
	        StmtIterator si = results.listStatements(root, null, (RDFNode)null);
	        while (si.hasNext()) {
	            RDFNode n = si.next().getObject();
	            if (n.isURIResource()) varAllocation.add( (Resource)n );
	        }
	    }
		return varAllocation;
	}

	private String buildLabelQuery( Set<Resource> varAllocation ) {
		StringBuilder labelQuery = new StringBuilder();
	    labelQuery.append( "PREFIX rdfs: <" + RDFS.getURI() + ">\n" );
	    labelQuery.append( "SELECT ?resource ?label\n" );
	    labelQuery.append( "WHERE {\n" );
	    String union = "";
	    for (Resource r: varAllocation) 
	    	if (!r.hasProperty(RDFS.label)){
	    		labelQuery.append( union );
	    		labelQuery.append( "{ ?resource rdfs:label ?label. FILTER(?resource = <" + r.getURI() + ">) }\n");
	    		union = " UNION ";
	    	}
	    labelQuery.append(" }\n");
//	    System.err.println( ">> " + labelQuery );
		return labelQuery.toString();
	}

	private void runLabelQuery(Source source, APIResultSet results,	Set<Resource> varAllocation, String labelQuery) {
		// TODO remove
	    // System.out.println("Running label expansion query: " + labelQuery);
	    
	    Query q = null;
	    try {
	        q = QueryFactory.create(labelQuery);
	    } catch (Exception e) {
	        throw new APIException("Internal error building label query: " + labelQuery, e);
	    }
	    QueryExecution exec = source.execute(q);
	
	    try {
	        // Run the select and find the matches
	        ResultSet rs = exec.execSelect();
	        while (rs.hasNext()) {
	        	QuerySolution s = rs.next();
//	        	System.err.println( ">> solution: " + s );
	        	Resource r = s.getResource( "resource" );
	        	String l = s.getLiteral("label").getLexicalForm();
	        	results.add( r, RDFS.label, l );
	        }
//	        if (rs.hasNext()) {
//	            QuerySolution soln = rs.next();
//	            for (Map.Entry<String, Resource> e : varAllocation.entrySet()) {
//	                String var = e.getKey();
//	                RDFNode label = soln.get( var.substring(1) );
//	                if (label != null) {
//	                    results.add(e.getValue(), RDFS.label, label);
//	                }
//	            }
//	        }
	        exec.close();
	    } catch (Throwable t) {
	        exec.close();
	        throw new APIException("Query execution problem on label fetching: " + t, t);
	    }
	}
}
