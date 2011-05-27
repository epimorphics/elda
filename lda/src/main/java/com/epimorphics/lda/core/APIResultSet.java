/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$

    File:        ResultSet.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.lda.support.LanguageFilter;
import com.epimorphics.lda.vocabularies.ELDA;
import com.epimorphics.lda.vocabularies.OpenSearch;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Wrapper for the results of an API query before rendering.
 * A ResultSet is an ordered list of results and an associated 
 * RDF graph.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIResultSet {

	protected Resource root;
	protected String contentLocation;

	protected final List<Resource> results;
    protected final boolean isCompleted;
    protected final Model model;
    protected final String detailsQuery;
    
    public String getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    public APIResultSet(Graph graph, List<Resource> results, boolean isCompleted, String detailsQuery) {
        model = ModelFactory.createModelForGraph( graph );
        Model resultsModel = results.get(0).getModel();
		setUsedPrefixes(resultsModel);
        this.results = results;
        this.isCompleted = isCompleted;
        this.detailsQuery = detailsQuery;
        if (!results.isEmpty())
            this.root = results.get(0).inModel(model);
    }

    /**
        Set prefixes for the namespaces of terms that Elda uses
        in its generated models.
    */
	private void setUsedPrefixes( Model resultsModel ) {
		model.setNsPrefixes( resultsModel );
        model.setNsPrefix( "rdf", RDF.getURI() );
        model.setNsPrefix( "rdfs", RDFS.getURI() );
        model.setNsPrefix( "dct", DCTerms.getURI() );
        model.setNsPrefix( "os", OpenSearch.getURI() );
        model.setNsPrefix( "sparql", SPARQL.NS );
        model.setNsPrefix( "doap", DOAP.NS );
        model.setNsPrefix( "xhv", XHV.getURI() );
        model.setNsPrefix( "opmv", ELDA.COMMON.NS );
	}

    /**
        Answer the model this resultset wraps.
    */
    public Model getModel() {
    	return model;
    }

    public long modelSize() {
		return model.size();
	}
    
    public void setNsPrefixes( PrefixMapping pm ) {
    	model.setNsPrefixes( pm );
    }
    
    public List<Resource> getResultList() {
        return results;
    }
    
    /**
        Answer the query string, if available, used to get the details
        of the values of properties of the selected items.
    */
    public String getDetailsQuery() {
    	return detailsQuery;
    }

    public void setRoot(Resource root) {
        this.root = root;
    }
    
    public Resource getRoot() {
        return root;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    /**
     * Generate and return a new copy of the model filtered to only include
     * statements reachable from the results via allowed properties in the given set.
     * Will not include any root resource, need to create page information after filtering.
     * @param string 
     */
    public APIResultSet getFilteredSet( View t, String languages ) {
    	Model m = t.applyTo( model, results );
    	if (languages != null) LanguageFilter.filterByLanguages( m, languages.split(",") );
        m.setNsPrefixes( model );
        List<Resource> mappedResults = new ArrayList<Resource>();
        for (Resource r : results) mappedResults.add( r.inModel(m) );
        return new APIResultSet( m.getGraph(), mappedResults, isCompleted, detailsQuery );
    }

	/**
     * Return a new result set with this one as its initial content 
     * but where additions to this model do not affect the source
     */
    @Override public APIResultSet clone() {
        // Dynamic cloning should be a lot better but strangely in fails
        // Perhaps there is a delete going on somewhere that I've missed
//        Graph additions = ModelFactory.createDefaultModel().getGraph();
//        Graph cloneGraph = new Union(additions, graph);
        Model temp = ModelFactory.createDefaultModel();
        temp.add( model );
        Graph cloneGraph = temp.getGraph();
        APIResultSet clone = new APIResultSet(cloneGraph, results, isCompleted, detailsQuery);
        clone.setRoot(root);
        clone.setContentLocation(contentLocation);
        return clone;
    }

	public StmtIterator listStatements( Resource S, Property P, RDFNode O) {
		return model.listStatements( S, P, O );
	}    
}

