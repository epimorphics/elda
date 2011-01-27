package com.epimorphics.sources.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.epimorphics.lda.sources.Source;
import com.epimorphics.lda.sources.SourceBase;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileManager;

public class TestSourceLocking {

	static class TestSource extends SourceBase implements Source {

		final List<String> history = new ArrayList<String>();

		Lock lock = new Lock() {

			@Override public void enterCriticalSection(boolean readLockRequested) {
				history.add( "LOCK" );
			}
	
			@Override public void leaveCriticalSection() {
				history.add( "UNLOCK" );
			}
			
		};
		
		final Model model = ModelFactory.createDefaultModel();
		
		@Override public void addMetadata( Resource meta ) {			
		}

		@Override public QueryExecution execute( Query q ) {
			QueryExecution qe = QueryExecutionFactory.create( q, model );
			history.add( "QE_CREATE" );
			return wrapped( qe );
		}

		private QueryExecution wrapped( final QueryExecution qe ) {
			return new QueryExecution() {
				
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
					return qe.execDescribe();
				}
				
				@Override public Model execDescribe() {
					return qe.execDescribe();
				}
				
				@Override public Model execConstruct(Model model) {
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
					qe.close();
				}
				
				@Override public void abort() {
					qe.abort();
				}
			};
		}

		@Override public Lock getLock() {
			return lock;
		}
	}
	
	@Test public void testMe() {
		TestSource s = new TestSource();
		Query q = QueryFactory.create( "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}" );
		s.executeConstruct( q );
		assertEquals( CollectionUtils.list( "LOCK", "QE_CRTEATE", "CONSTRUCT", "UNLOCK" ), s.history );
	}
}
