/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.sources.test;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.*;
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

    @Override public void setTimeout(long timeout) {
        qe.setTimeout( timeout );
    }

    @Override public void setTimeout(long timeout, TimeUnit unit) {
        qe.setTimeout( timeout, unit );
    }

    @Override public void setTimeout(long arg0, long arg1) {
        qe.setTimeout( arg0, arg1 );
    }

    @Override public void setTimeout(long time1, TimeUnit unit1, long time2, TimeUnit unit2) {
        qe.setTimeout( time1, unit1, time2, unit2 );
    }

	@Override public Query getQuery() {
		return qe.getQuery();
	}

	@Override public Iterator<Triple> execConstructTriples() {
		return qe.execConstructTriples();
	}

	@Override public Iterator<Triple> execDescribeTriples() {
		return qe.execDescribeTriples();
	}

	@Override public long getTimeout1() {
		return qe.getTimeout1();
	}

	@Override public long getTimeout2() {
		return qe.getTimeout2();
	}
}