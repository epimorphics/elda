package com.epimorphics.lda.shortnames.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.util.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.hp.hpl.jena.rdf.model.*;

public class TestShortNames {
	
	@Test public void ensureUndeclatedURIUsesPrefix() {
		Model m = ModelIOUtils.modelFromTurtle( "@prefix p: <http://example.com/ns#>. p:a p:thing p:b; p:other p:d; p:thong p:c." );
		Map<String, String> mm = 
			new CompleteContext(CompleteContext.Mode.RoundTrip, new Context( m ), m )
			.Do1(m); 
		assertEquals( "p_thing", mm.get( m.expandPrefix( "p:thing" ) ) );
	}
	
	@Test public void ensureConfigShortnameIsUsed() {
		Model empty = ModelFactory.createDefaultModel();
		Model m = ModelIOUtils.modelFromTurtle
			( "@prefix p: <http://example.com/ns#>."
			+ "\np:a p:thing p:b; p:other p:d; p:thong p:c." 
			+ "\np:thing rdfs:label 'labelled'; rdfs:range p:Thing."
			);
		Context c = new Context( m ); 
		Map<String, String> mm = new CompleteContext(CompleteContext.Mode.RoundTrip, c, m).Do();
		assertEquals( "labelled", mm.get( m.expandPrefix( "p:thing" ) ) );
	}
	
	@Test public void ensureApiLabelWinsOverRDFSLabel() {
		Model empty = ModelFactory.createDefaultModel();
		Model m = ModelIOUtils.modelFromTurtle
			( "@prefix p: <http://example.com/ns#>."
			+ "\np:a p:thing p:b; p:other p:d; p:thong p:c." 
			+ "\np:thing rdfs:label 'labelled'." 
			+ "\np:thing api:label 'REALLY_labelled'."
			);
		Context c = new Context( m ); 		
		Map<String, String> mm = new CompleteContext(CompleteContext.Mode.RoundTrip, c, m).Do();
		assertEquals( "REALLY_labelled", mm.get( m.expandPrefix( "p:thing" ) ) );
	}	
	
	@Test public void ensure_sensitive_result_without_prefix_is_converted() {
		Model meta = ModelIOUtils.modelFromTurtle( "<eh:/A> foaf:primaryTopic <eh:/B>." );
		Model object = ModelIOUtils.modelFromTurtle( "<eh:/B> <http://example.com/result> <eh:/C>." );
		ensure_result_converted("unknown_httpXexampleDcomSZresult ", meta, object);
	}
	
	@Test public void ensure_sensitive_result_with_prefix_is_converted() {
		Model meta = ModelIOUtils.modelFromTurtle( "<eh:/A> foaf:primaryTopic <eh:/B>." );
		Model object = ModelIOUtils.modelFromTurtle( "<eh:/B> <http://example.com/result> <eh:/C>." );
		object.setNsPrefix( "my", "http://example.com/" );
		ensure_result_converted("<my_result ", meta, object );
	}

	private void ensure_result_converted(String expectContains, Model meta, Model object ) {
		SNS sns = new SNS( "" );
		Renderer r = new XMLRenderer( sns );
	//
		Times t = new Times();
		Resource rx = object.createResource( "eh:/A" );
		List<Resource> results = new ArrayList<Resource>(); results.add( rx );
		APIResultSet rs = new APIResultSet(object.getGraph(), results, true, false, "detailsQuery", new View() );
		MergedModels mm = rs.getModels();
		mm.getMetaModel().add( meta );
		
		Map<String, String> termBindings =
			new CompleteContext(CompleteContext.Mode.RoundTrip, sns.asContext(), mm.getMergedModel() )
			.include( mm.getMergedModel() )
			.Do()
			;
	//
		Renderer.BytesOut bo = r.render(t, new Bindings(), termBindings, rs);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bo.writeAll(t, bos);
		assertContains( expectContains, bos.toString() );
	}

	private void assertContains(String sub, String subject) {
		if (subject.contains( sub )) return;
		fail( "Expected '" + sub + "' to be present in '" + subject + "'" );		
	}
}
