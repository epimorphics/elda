/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$

    File:        ResultSet.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
*/

package com.epimorphics.lda.core;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;

/**
 * Wrapper for the results of an API query before rendering.
 * A ResultSet is an ordered list of results and an associated 
 * RDF graph.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class APIResultSet extends ModelCom  {

    protected List<Resource> results;
    protected Resource root;
    protected boolean isCompleted;
    protected String contentLocation;
    
    public String getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    public APIResultSet(Graph graph, List<Resource> results, boolean isCompleted) {
        super(graph);
        this.results = results;
        this.isCompleted = isCompleted;
        if (!results.isEmpty())
            this.root = results.get(0).inModel(this); 
    }

    public List<Resource> getResultList() {
        return results;
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
     */
    public APIResultSet getFilteredSet( View t ) {
    	Model m = t.applyTo( this, results );
        m.setNsPrefixes(this);
//        System.err.println( ">> applying template " + t );
//        System.err.println( ">> to this:" );
//        this.write( System.err, "TTL" );
//        System.err.println( ">> produces this:" );
//        m.write( System.err, "TTL" );
        List<Resource> mappedResults = new ArrayList<Resource>();
        for (Resource r : results) mappedResults.add( r.inModel(m) );
        return new APIResultSet( m.getGraph(), mappedResults, isCompleted );
    }
    
    /**
     * Return a new result set with this one as its initial content 
     * but where additions to this model do not affect the source
     */
    public APIResultSet clone() {
        // Dynamic cloning should be a lot better but strangely in fails
        // Perhaps there is a delete going on somewhere that I've missed
//        Graph additions = ModelFactory.createDefaultModel().getGraph();
//        Graph cloneGraph = new Union(additions, graph);
        Model temp = ModelFactory.createDefaultModel();
        temp.add( this );
        Graph cloneGraph = temp.getGraph();
        APIResultSet clone = new APIResultSet(cloneGraph, results, isCompleted);
        clone.setRoot(root);
        clone.setContentLocation(contentLocation);
        return clone;
    }
    
}

