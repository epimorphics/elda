package temp;

import java.io.*;
import java.util.*;

import junit.framework.Assert;

import org.junit.Test;

import com.epimorphics.jsonrdf.*;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.renderers.*;
import com.epimorphics.lda.renderers.Renderer.UTF8;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.tests.SNS;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.StreamUtils;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestJSONIssue {
	
	// "2013-10-02T08:37:34Z"^^xsd:date

	String XSD_Date = XSDDatatype.XSDdate.getURI();

	Model model = ModelFactory.createDefaultModel();
	Literal oopsy = model.createTypedLiteral( "2013-10-02T08:37:34Z", XSD_Date );
//	Literal oopsy = model.createTypedLiteral( "2013-10-02", XSD_Date );
	Resource item = model.createResource( "eh:/it" ).addProperty( RDF.value, oopsy );
	List<Resource> items = CollectionUtils.list( item );

	@Test public void testJSON() throws IOException {
		ReadContext context = new FakeContext();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Writer writer = StreamUtils.asUTF8( os );
		Encoder.getForOneResult( context ).encodeRecursive( model, items, writer, true );
		writer.flush();
		String content = UTF8.toString( os );
		Assert.assertNotNull(content);
	}
	
	@Test public void testXML() throws IOException {
		XMLRenderer x = new XMLRenderer( new SNS("value=" + RDF.value.getURI() + ";it=eh:/it") );
		Times t = new Times();
		Bindings rc = new Bindings();
		Map<String, String> termBindings = new HashMap<String, String>();
	//
		Resource root = model.createResource( "eh:/root" );
		RDFList l = model.createList( new RDFNode[] {item} );
		model.add( root, API.items, l );
	//
		APIResultSet results = new APIResultSet
			( model.getGraph()
			, items
			, true
			, false
			, "the details query"
			, View.DESCRIBE
			);
		results.setRoot(root);
	//
		results.getModels().getMetaModel().add( root, API.items, l );
	//
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Renderer.BytesOut bo = x.render(t, rc, termBindings, results);
		bo.writeAll(t, os);
	//
		String content = UTF8.toString( os );
	//
		Assert.assertNotNull(content);	
	}

	
	private static class FakeContext implements ReadContext {
		@Override
		public ContextPropertyInfo findProperty(Property p) {
			return new ContextPropertyInfo(p.getURI(), p.getLocalName());
		}

		@Override public boolean isSortProperties() {
			return true;
		}

		@Override public String getNameForURI(String uri) {
			// TODO Auto-generated method stub
			throw new RuntimeException();
		}

		@Override public String getURIfromName(String code) {
			// TODO Auto-generated method stub
			throw new RuntimeException();
		}

		@Override public String getBase() {
			return "";
		}

		@Override public String forceShorten(String uri) {
			// TODO Auto-generated method stub
			throw new RuntimeException();
		}

		@Override public ContextPropertyInfo getPropertyByName(String name) {
			return new ContextPropertyInfo("eh:/wossname/" + name, name);
		}
	}
}
