package com.epimorphics.lda.support.tests;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.support.CycleFinder;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestCycleFinder {
	
	@Test public void returnsNoCycleFromPlainStatement() {
		Model m = ModelIOUtils.modelFromTurtle( ":x rdf:type :T." );
		Resource x = m.createResource( m.expandPrefix( ":x" ) );
		Set<Resource> loops = CycleFinder.findCycles( x );
		assertEquals( CollectionUtils.set(), loops );
	}	
	
	@Test public void returnsNoCycleFromChainOfStatements() {
		Model m = ModelIOUtils.modelFromTurtle( ":x :P :y. :y :Q :z." );
		Resource x = m.createResource( m.expandPrefix( ":x" ) );
		Set<Resource> loops = CycleFinder.findCycles( x );
		assertEquals( CollectionUtils.set(), loops );
	}
	
	@Test public void returnsNoCycleFromParallelStatements() {
		Model m = ModelIOUtils.modelFromTurtle( ":x :P :y. :x :Q :z." );
		Resource x = m.createResource( m.expandPrefix( ":x" ) );
		Set<Resource> loops = CycleFinder.findCycles( x );
		assertEquals( CollectionUtils.set(), loops );
	}
	
	@Test  public void returnsSingleStatementCycle() {
		Model m = ModelIOUtils.modelFromTurtle( ":x :P :x." );
		Resource x = m.createResource( m.expandPrefix( ":x" ) );
		Set<Resource> loops = CycleFinder.findCycles( x );
		assertEquals( CollectionUtils.set(x), loops );
	}
	
	@Test  public void returnsMultiStatementCycle() {
		Model m = ModelIOUtils.modelFromTurtle( ":x :P :y. :y :Q :x." );
		Resource x = m.createResource( m.expandPrefix( ":x" ) );
		Resource y = m.createResource( m.expandPrefix( ":y" ) );
		Set<Resource> loops = CycleFinder.findCycles( x );
		assertEquals( CollectionUtils.set(x, y), loops );
	}
	
	@Test  public void returnsDeferedMultiStatementCycle() {
		Model m = ModelIOUtils.modelFromTurtle( ":x :P :y. :y :Q :z. :z :R :y." );
		Resource x = m.createResource( m.expandPrefix( ":x" ) );
		Resource y = m.createResource( m.expandPrefix( ":y" ) );
		Resource z = m.createResource( m.expandPrefix( ":z" ) );
		Set<Resource> loops = CycleFinder.findCycles( x );
		assertEquals( CollectionUtils.set(y, z), loops );
	}

}
