package com.epimorphics.lda.renderers.velocity.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.URLforResource;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.renderers.velocity.VelocityRenderer;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.ELDA.COMMON;
import com.epimorphics.lda.vocabularies.ELDA.DOAP_EXTRAS;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.SPARQL;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestRecursionDetectionInProvidedTemplate {

	final Model model = ModelFactory.createDefaultModel();
	
	final URLforResource x = new URLforResource() {

		@Override public URL asResourceURL(String u) {
			File dot = new File(System.getProperty("user.dir"));			
			File full = new File(dot, u);
		//
			try {
				return new URL("file:" + full.toString());
			} catch (MalformedURLException e) {
				throw new WrappedException(e);			
			}
		}
		
	};
	
	@Test public void testTecursionDetection() {
		MediaType mt = MediaType.TEXT_HTML;
		Bindings b = new Bindings(x);
		
	// hack the velocity root. how do we do better?
		b.put("_velocityRoot", "../elda-standalone/src/main/webapp/lda-assets/vm/");	
		b.put("_resourceRoot", "../elda-standalone/src/main/webapp/lda-assets");			
	//
		Resource config = model.createResource( "eh:/config" );
		Renderer r = new VelocityRenderer( mt, b, config );
	//
		Model resultModel = ModelFactory.createDefaultModel();
		Resource item = resultModel.createResource( "eh:/item" );
		item.addProperty( RDFS.label, "this is my label." );
		item.addProperty( RDF.value, item );
	//
		Resource page = resultModel.createResource( "eh:/page" );
		Resource x = resultModel.createResource( "eh:/x" );
		page.addProperty( RDF.type, API.Page );
		page.addProperty( API.wasResultOf, x );
		
		addProperty( x, API.selectionResult, SPARQL.query, API.value, resultModel.createLiteral("SR") );
		addProperty( x, API.viewingResult, SPARQL.query, API.value, resultModel.createLiteral("VR") );
		addProperty( x, API.processor, COMMON.software, API.label, resultModel.createLiteral("Elda") );
		addProperty( x, API.processor, COMMON.software, DOAP_EXTRAS.releaseOf, DOAP.homepage, resultModel.createResource( "eh:/homePage" ) );
	//
		addProperty( x, API.variableBinding, v( resultModel, "_resourceRoot", "../elda-standalone/src/main/webapp/lda-assets") );
		addProperty( x, API.variableBinding, v( resultModel, "_rootPath", "standalone") );
		addProperty( x, API.variableBinding, v( resultModel, "_page", "1") );
		addProperty( x, API.variableBinding, v( resultModel, "_view", "all") );
		addProperty( x, API.variableBinding, v( resultModel, "_properties", "*") );
	//
		Times t = new Times();
		Map<String, String> termBindings = new HashMap<String, String>();
		APIResultSet results = new APIResultSet
			( resultModel.getGraph()
			, CollectionUtils.list( item )
			, true
			, true
			, "detailsQuery"
			, View.BASIC
			);
	//
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BytesOut bo = r.render(t, b, termBindings, results);
		bo.writeAll(t, bos);
		// System.err.println( ">> content: " + bos.toString() );
	}
	
	private RDFNode v(Model m, String name, String value) {
		return m.createResource()
			.addProperty(API.label, name)
			.addProperty(API.value, value)
			;
	}

	protected void addProperty( Resource x, RDFNode... nodes ) {
		int i = nodes.length;
		RDFNode value = nodes[--i];
		while (i > 1) {
			Property p = nodes[--i].as(Property.class);
			Resource r = x.getModel().createResource();
			r.addProperty(p, value);
			value = r;
		}
		x.addProperty( nodes[0].as(Property.class), value );
	}
	
}
