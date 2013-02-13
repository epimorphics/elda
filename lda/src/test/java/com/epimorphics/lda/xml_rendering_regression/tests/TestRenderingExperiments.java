package com.epimorphics.lda.xml_rendering_regression.tests;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.core.EndpointMetadata;
import com.epimorphics.lda.core.SetsMetadata;
import com.epimorphics.lda.query.WantsMetadata;
import com.epimorphics.lda.renderers.Factories;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.NameMap;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.StandardShortnameService;
import com.epimorphics.lda.sources.SparqlSource;
import com.epimorphics.lda.specs.EndpointDetails;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.DOMUtils;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestRenderingExperiments extends XMLTestCase {
	
	static class Blob {
		
		private static final String goldRoot = "src/test/resources/xml_gold/";
		
		final Model objectModel;
		
		Blob( Model objectModel ) {
			this.objectModel = objectModel;
		}
		
		static Blob load( String name ) {
			Model objectModel = FileManager.get().loadModel( goldRoot + name + "/object_model.ttl" );
			return new Blob( objectModel );
		}
	}
	
	public void testIt() throws ParserConfigurationException, SAXException, IOException {
		Blob b = Blob.load( "probe" );
		Model objectModel = b.objectModel;
	//	
		final MergedModels mm = new MergedModels( objectModel );
	//
		SetsMetadata setsMeta = new SetsMetadata() {
			@Override public void setMetadata(String type, Model meta) {
				mm.getMetaModel().add( meta );
			}
		};
	//
		WantsMetadata wantsMeta = new WantsMetadata() {
			@Override public boolean wantsMetadata(String name) {
				return true;
			}
		};
	//
		EndpointDetails details = new EndpointDetails() {
			@Override public boolean isListEndpoint() {
				return true;
			}
			
			@Override public boolean hasParameterBasedContentNegotiation() {
				return false;
			}
		};
	//
		Resource X = objectModel.createResource( "fake:X" );
		Resource Y = objectModel.createResource( "fake:Y" );
		List<Resource> resultList = CollectionUtils.list( X, Y );
	//
		URI ru = URIUtils.newURI( "http://localhost:8080/elda/something/or/other" );
		Resource uriForDefinition = mm.getMetaModel().createResource( "http://localhost:8080/elda/meta/something/or/other" );
	//
		Resource thisMetaPage = mm.getMetaModel().createResource( ru.toString() );
		
		EndpointMetadata.addAllMetadata
	    	( mm
	    	, ru
	    	, uriForDefinition
	    	, new Bindings()
	    	, new NameMap()
	    	, true
	    	, thisMetaPage
	    	, 0
	    	, 10
	    	, false
	    	, resultList
	    	, setsMeta
	    	, wantsMeta
	    	, "select query"
	    	, "view query"
	    	, new SparqlSource( null, "sparql:endpoint", null )
	    	, CollectionUtils.set( "aView" )
	    	, CollectionUtils.set( new Factories.FormatNameAndType( "format", "x/y" ) )
	    	, details
	    	); 
				
		ShortnameService sns = new StandardShortnameService();	
		

		Resource root = mm.getMetaModel().createResource( "some:root" );

		Resource pt = objectModel.createResource( "primary:topic" );
		
		pt.addProperty( RDFS.label, "primary" );
		
		root.addProperty( FOAF.primaryTopic, pt );
		
		Document d = DOMUtils.newDocument();
		
		XMLRenderer r = new XMLRenderer( sns );
		r.renderInto( root, mm, d, false );
		
		Document expected = parse("<result href='some:root' version='0.2' format='linked-data-api'><primaryTopic href='primary:topic'><label>primary</label></primaryTopic></result>");
		assertXMLEqual( expected, d );
	}

	public Document parse( String data ) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		return dBuilder.parse( new InputSource( new StringReader( data ) ) );
	}

}
