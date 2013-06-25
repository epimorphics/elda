package com.epimorphics.lda.renderers.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.util.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.Context;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.renderers.JSONRenderer;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Test that the JSON renderer doesn't corrupt the Context with
    newly required short names. (The JSON writer is still free to
    do so, but the JSON renderer takes a copy of the context.)
*/
public class TestJsonRenderer {
	
	@Test public void testDoesntUpdateContext() {
		Bindings b = new Bindings();
		Model model = ModelIOUtils.modelFromTurtle( "<fake:root> <fake:predicate> 17 ." );
		Resource root = model.createResource( "fake:root" );
		Context given = new Context();
		new JSONRenderer( null ).renderAndDiscard( b, model, root, given );
		Set<String> allShortNames = given.preferredNames();
		assertEquals( "rendering should not update the context", Collections.EMPTY_SET, allShortNames );
	}
	
	static final Model tinySpec = ModelIOUtils.modelFromTurtle
	( "<eh:/API> a api:API"
			+ "\n; api:endpoint <eh:/endpoint>"
			+ "\n; api:sparqlEndpoint <eh:/not-really>"
			+ "\n."
			+ "\n<eh:/endpoint> a api:ListEndpoint"
			+ "\n; api:uriTemplate '/foo'"
			+ "\n." 
			+ "\n"
	);
	
	@Test public void testCallbackConstruction() {
		Bindings b = new Bindings();
		String jsonNoCallback = runTinyRenderer( b );
		b.put( "callback", "flounce" );
		String jsonWithCallback = runTinyRenderer( b );
		if (!jsonWithCallback.equals( "flounce(" + jsonNoCallback + ")" )) {
			fail("JSON callback failure: did not wrap output with function name." );
		}
	}

	private String runTinyRenderer( Bindings b ) {
		JSONRenderer jr = createTinyRenderer();
		Times t = new Times();
		Model m = ModelIOUtils.modelFromTurtle( "<fake:root> <fake:predicate> 17 ." );
		Resource root = m.createResource( "fake:root" );
		List<Resource> results = CollectionUtils.list( root );
		APIResultSet rs = new APIResultSet( m.getGraph(), results, true, false, "detailsQuery", new View() );
		Renderer.BytesOut bo = jr.render( t, b, new HashMap<String, String>(), rs );
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bo.writeAll(t, bos);
		return bos.toString();
	}

	private JSONRenderer createTinyRenderer() {
		Resource specRoot = tinySpec.createResource( "eh:/API" );
		APISpec s = new APISpec( null, specRoot, null );
		Resource epResource = tinySpec.createResource( "eh:/endpoint" );
		APIEndpointSpec spec = new APIEndpointSpec( s, s, epResource );
		APIEndpoint ep = new APIEndpointImpl( spec );
		JSONRenderer jr = new JSONRenderer( ep );
		return jr;
	}

}
