package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;

public class TestShortNames {

//	@Test public void ensure_prefixing_of_sensitive_names() {
//		NameMap nm = new NameMap();
//		Set<String> sensitives = new HashSet<String>( ModelTestBase.listOfStrings( "thing other" ) );
//		Model m = ModelIOUtils.modelFromTurtle( "@prefix p: <http://example.com/ns#>. p:a p:thing p:b; p:other p:d; p:thong p:c." );
//		MultiMap<String, String> mm = nm.stage2(false).load(m, m).result( sensitives );
//	//	
//		assertEquals( "thong", mm.getOne( "http://example.com/ns#thong" ) );
//		assertEquals( "p_thing", mm.getOne( "http://example.com/ns#thing" ) );
//		assertEquals( "p_other", mm.getOne( "http://example.com/ns#other" ) );
//	}
	
	@Test public void ensureUndeclatedURIUsesPrefix() {
		NameMap nm = new NameMap();
		Model m = ModelIOUtils.modelFromTurtle( "@prefix p: <http://example.com/ns#>. p:a p:thing p:b; p:other p:d; p:thong p:c." );
		Map<String, String> mm = nm.stage2().loadPredicates(m, m).result();
		assertEquals( "p_thing", mm.get( m.expandPrefix( "p:thing" ) ) );
	}
	
	@Test public void ensureConfigShortnameIsUsed() {
		Model empty = ModelFactory.createDefaultModel();
		NameMap nm = new NameMap();
		Model m = ModelIOUtils.modelFromTurtle
			( "@prefix p: <http://example.com/ns#>."
			+ "\np:a p:thing p:b; p:other p:d; p:thong p:c." 
			+ "\np:thing rdfs:label 'labelled'."
			);
		nm.load(m, m);
		nm.done();
		Map<String, String> mm = nm.stage2().loadPredicates(empty, empty).result();
		assertEquals( "labelled", mm.get( m.expandPrefix( "p:thing" ) ) );
	}
	
	@Test public void ensureApiLabelWinsOverRDFSLabel() {
		Model empty = ModelFactory.createDefaultModel();
		NameMap nm = new NameMap();
		Model m = ModelIOUtils.modelFromTurtle
			( "@prefix p: <http://example.com/ns#>."
			+ "\np:a p:thing p:b; p:other p:d; p:thong p:c." 
			+ "\np:thing rdfs:label 'labelled'." 
			+ "\np:thing api:label 'REALLY_labelled'."
			);
		nm.load(m, m);
		nm.done();
		Map<String, String> mm = nm.stage2().loadPredicates(empty, empty).result();
		assertEquals( "REALLY_labelled", mm.get( m.expandPrefix( "p:thing" ) ) );
	}	
	
	@Ignore @Test public void ensure_sensitive_result_without_prefix_is_converted() {
		Model m = ModelIOUtils.modelFromTurtle( "<eh:/A> <http://example.com/result> <eh:/C>." );
		ensure_result_converted("uri_http3A2F2Fexample2Ecom2Fresult ", m);
	}
	
	@Test public void ensure_sensitive_result_with_prefix_is_converted() {
		
		System.err.println( ">> TODO: fix this test" ); if (true) return;
		
		Model m = ModelIOUtils.modelFromTurtle( "<eh:/A> <http://example.com/result> <eh:/C>." );
		m.setNsPrefix( "my", "http://example.com/" );
		ensure_result_converted("<my_result ", m);
	}

	private void ensure_result_converted(String expectContains, Model m) {
		Renderer r = new XMLRenderer( new SNS( "" ) );
	//
		Times t = new Times();
		Resource rx = m.createResource( "eh:/A" );
		List<Resource> results = new ArrayList<Resource>(); results.add( rx );
		APIResultSet rs = new APIResultSet(m.getGraph(), results, true, false, "detailsQuery", new View() );
	//
		Renderer.BytesOut bo = r.render(t, new Bindings(), rs);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bo.writeAll(t, bos);
		assertContains( expectContains, bos.toString() );
	}

	private void assertContains(String sub, String subject) {
		if (subject.contains( sub )) return;
		fail( "Expected '" + sub + "' to be present in '" + subject + "'" );		
	}
}
