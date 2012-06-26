package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.support.MultiMap;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.Model;
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
	
	@Test public void ensure_sensitive_result_is_converted() {
		Model m = ModelIOUtils.modelFromTurtle( "<eh:/A> <eh:/result> <eh:/C>." );
		Resource root = m.createResource( "eh:/root" ).addProperty( EXTRAS.forcePrefix, "result" );
		Renderer r = new XMLRenderer( new SNS( "" ), root );
	//
		Times t = new Times();
		Resource rx = m.createResource( "eh:/A" );
		List<Resource> results = new ArrayList<Resource>(); results.add( rx );
		APIResultSet rs = new APIResultSet(m.getGraph(), results, true, false, "detailsQuery" );
	//
		Renderer.BytesOut bo = r.render(t, new Bindings(), rs);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bo.writeAll(t, bos);
		assertContains( "<none_result ", bos.toString() );
	}

	private void assertContains(String sub, String subject) {
		if (subject.contains( sub )) return;
		fail( "Expected '" + sub + "' to be present in '" + subject + "'" );		
	}
}
