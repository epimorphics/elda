/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sources.test;

import java.util.List;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileManager;

/**
    A wrapper round QueryExecution that logs some of its actions
    into a history list, for later examination.
*/
public class QueryExecutionWithHistory implements QueryExecution {
	
	private final QueryExecution qe;
	private final List<String> history;
	
	QueryExecutionWithHistory(QueryExecution qe, List<String> history ) {
		this.qe = qe;
		this.history = history;
	}

	@Override public void setInitialBinding(QuerySolution binding) {
		qe.setInitialBinding(binding);
	}

	@Override public void setFileManager(FileManager fm) {
		qe.setFileManager(fm);
	}

	@Override public Dataset getDataset() {
		return qe.getDataset();
	}

	@Override public Context getContext() {
		return qe.getContext();
	}

	@Override public ResultSet execSelect() {
		return qe.execSelect();
	}

	@Override public Model execDescribe(Model model) {
		history.add( "DESCRIBE(model)" );
		return qe.execDescribe();
	}

	@Override public Model execDescribe() {
		history.add( "DESCRIBE" );
		return qe.execDescribe();
	}

	@Override public Model execConstruct(Model model) {
		history.add( "CONSTRUCT(model)" );
		return qe.execConstruct(model);
	}

	@Override public Model execConstruct() {
		history.add( "CONSTRUCT" );
		return qe.execConstruct();
	}

	@Override public boolean execAsk() {
		return qe.execAsk();
	}

	@Override public void close() {
		history.add( "CLOSE" );
		qe.close();
	}

	@Override public void abort() {
		qe.abort();
	}
}