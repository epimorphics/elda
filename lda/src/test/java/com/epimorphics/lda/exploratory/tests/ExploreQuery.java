package com.epimorphics.lda.exploratory.tests;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;

public class ExploreQuery {

	
	@Test public void testMe() {
		Model m = ModelIOUtils.modelFromTurtle( ":a :P :ap. :ap :Q 1 . :a :P :ar. :ar :R 2 ." );
		String qString = 
			"prefix : <http://www.epimorphics.com/tools/example#>"
			+ "\nconstruct {"
			+ "\n:a :P ?v1. ?v1 :Q ?v2."
			+ "\n:a :P ?v3. ?v3 :R ?v4."
			+ "\n}"
			+ "\nwhere{"
			+ "\n{:a :P ?v1 OPTIONAL {?v1 :Q ?v2}}"
			+ "\nUNION {:a :P ?v3 OPTIONAL {?v3 :R ?v4}}"
			+ "\n}"
			;
		Query q = QueryFactory.create( qString );
		QueryExecution qe = QueryExecutionFactory.create( q, m );
		Model c = qe.execConstruct();
		// c.write( System.out, "TTL" );
	}

	@Test public void testMe2() {
		Model m = ModelIOUtils.modelFromTurtle( ":a :P :ap. :ap :Q 1 . :a :P :ar. :ar :R 2 ." );
		String qString = 
			"prefix : <http://www.epimorphics.com/tools/example#>"
			+ "\nconstruct {"
			+ "\n:a :P ?v1. ?v1 :Q ?v2."
			+ "\n:a :P ?v1. ?v1 :R ?v3."
			+ "\n}"
			+ "\nwhere{"
			+ "\n{:a :P ?v1 OPTIONAL {?v1 :Q ?v2}}"
			+ "\nUNION {:a :P ?v1 OPTIONAL {?v1 :R ?v3}}"
			+ "\n}"
			;
		Query q = QueryFactory.create( qString );
		QueryExecution qe = QueryExecutionFactory.create( q, m );
		Model c = qe.execConstruct();
		// c.write( System.out, "TTL" );
	}
	
	@Test public void testMe3() {
		Model m = ModelIOUtils.modelFromTurtle( ":a :P :ap. :ap :Q 1 . :a :P :ar. :ar :R 2 ." );
		String qString = 
			"prefix : <http://www.epimorphics.com/tools/example#>"
			+ "\nconstruct {"
			+ "\n:a :P ?v1. ?v1 :Q ?v2."
			+ "\n:a :P ?v1. ?v1 :R ?v3."
			+ "\n}"
			+ "\nwhere{"
			+ "\n{:a :P ?v1. ?v1 :Q ?v2}"
			+ "\nUNION {:a :P ?v1. ?v1 :R ?v3}"
			+ "\n}"
			;
		Query q = QueryFactory.create( qString );
		QueryExecution qe = QueryExecutionFactory.create( q, m );
		Model c = qe.execConstruct();
		// c.write( System.out, "TTL" );
	}

}
