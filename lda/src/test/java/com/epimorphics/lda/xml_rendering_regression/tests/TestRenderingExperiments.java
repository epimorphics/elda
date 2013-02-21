package com.epimorphics.lda.xml_rendering_regression.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestRenderingExperiments extends XMLTestCase {
	
	public void testProbe() throws Exception {
		testGolden( "probe" );
	}
	
	public void testMiniEdA() throws Exception {
		testGolden( "mini-ed-A" );
	}
	
	public void testMiniEdB() throws Exception {
		testGolden( "mini-ed-B" );
	}
	
	public void testLrA() throws Exception {
		testGolden( "lr-A" );
	}
	
	public void testLrB() throws Exception {
		testGolden( "lr-B" );
	}
	
	public void testLrCB() throws Exception {
		testGolden( "lr-C" );
	}
	
	public void testNwqCB() throws Exception {
		testGolden( "bwq-A" );
	}
	
	public void testBwqCB() throws Exception {
		testGolden( "bwq-B" );
	}
	
	public void testBwqC() throws Exception {
		testGolden( "bwq-C" );
	}
	
	public void testBwqD() throws Exception {
		testGolden( "bwq-D" );
	}
	
	public void testBwqE() throws Exception {
		testGolden( "bwq-E" );
	}
	
	public void testAusA() throws Exception {
		testGolden( "aus-A" );
	}
	
	public void testAusB() throws Exception {
		testGolden( "aus-B" );
	}
	
	public void testGolden( String goldName ) throws Exception {
		GoldXMLTest b = GoldXMLTest.load( goldName );
	//	
		final MergedModels mm = new MergedModels( b.objectModel );
		mm.getMetaModel().add( b.metaModel );
		Resource root = mm.getMetaModel().createResource( b.root_uri );
		Document d = DOMUtils.newDocument();
	//
		XMLRenderer r = new XMLRenderer( b.sns );
		r.renderInto( root, mm, d, false );
	//
		Document expected = parse( b.expected_xml );
		Diff myDiff = new Diff( expected, d );

//		writeDocument( "rendered.xml", d);
//		
//		writeDocument( "expected.xml", expected );
		
		assertTrue( myDiff.toString(), myDiff.similar() );		
	}

	public void writeDocument(String name, Document d) throws Exception {
		OutputStream os = new FileOutputStream( new File( "/tmp/document-" + name ) );
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
		transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource( d );
		StreamResult result = new StreamResult( os );
		transformer.transform( source, result );
		os.close();
	}

	public Document parse( String data ) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document parsed = dBuilder.parse( new InputSource( new StringReader( data ) ) );
		// removeWhitespaceNodes( parsed.getDocumentElement() );
		return parsed;
	}
	
	public static void removeWhitespaceNodes( Element e ) {
		NodeList children = e.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node child = children.item(i);
			if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
				e.removeChild(child);
			} else if (child instanceof Element) {
				removeWhitespaceNodes((Element) child);
			}
		}
	}

}



//		SetsMetadata setsMeta = new SetsMetadata() {
//@Override public void setMetadata(String type, Model meta) {
//mm.getMetaModel().add( meta );
//}
//};
////
//WantsMetadata wantsMeta = new WantsMetadata() {
//@Override public boolean wantsMetadata(String name) {
//return true;
//}
//};
////
//EndpointDetails details = new EndpointDetails() {
//@Override public boolean isListEndpoint() {
//return true;
//}
//
//@Override public boolean hasParameterBasedContentNegotiation() {
//return false;
//}
//};
////
//List<Resource> resultList = new ArrayList<Resource>();
//String pre = "http://landregistry.data.gov.uk/data/ppi/transaction/";
//resultList.add( objectModel.createResource( pre + "AB393402-2265-4A0A-81CA-64523DF04878" ) );
//resultList.add( objectModel.createResource( pre + "5001880F-F082-4424-9B11-F5771FBA6006" ) );
//resultList.add( objectModel.createResource( pre + "056E052F-4193-4FBF-9956-24D46E9CCBA5" ) );
//resultList.add( objectModel.createResource( pre + "3B3E9500-D094-4747-9434-542A81F1990B" ) );
//resultList.add( objectModel.createResource( pre + "7235D3F7-3B24-42C2-ADEE-20BA628886A2" ) );
//resultList.add( objectModel.createResource( pre + "9DA12BDB-3497-49F9-93DE-47AE922A2FD7" ) );
//resultList.add( objectModel.createResource( pre + "407F87D5-E54B-4BA3-8CB6-455C96109B63" ) );
//resultList.add( objectModel.createResource( pre + "4EC1D2A7-B6E4-4D08-8A36-61BCB38AD8B8" ) );
//resultList.add( objectModel.createResource( pre + "01D708E7-F982-4A19-9A55-AEA000CB4426" ) );
//resultList.add( objectModel.createResource( pre + "4CE0C18F-C8F3-4897-87C4-9721E32BEE16" ) ); 
////
//URI ru = URIUtils.newURI( "http://landregistry.data.gov.uk/data/ppi/transaction" );
//Resource uriForDefinition = mm.getMetaModel().createResource( "http://landregistry.data.gov.uk/meta/data/ppi/transaction" );
////
//Resource thisMetaPage = mm.getMetaModel().createResource( ru.toString() );
////	
//Set<FormatNameAndType> formats = new HashSet<FormatNameAndType>();
//formats.add( new FormatNameAndType( "csv", "text/csv" ) );
//formats.add( new FormatNameAndType( "html", "text/html" ) );
//formats.add( new FormatNameAndType( "json", "application/json" ) );
//formats.add( new FormatNameAndType( "rdf", "application/rdf+xml" ) );
//formats.add( new FormatNameAndType( "text", "text/plain" ) );
//formats.add( new FormatNameAndType( "ttl", "text/turtle" ) );
//formats.add( new FormatNameAndType( "xml", "application/xml" ) );
////	
////
//PrefixMapping pm = PrefixMapping.Factory.create();
//Model maps = ModelFactory.createDefaultModel();
//
//maps
//.createResource( "http://landregistry.data.gov.uk/def/ppi/hasTransactionRecord" )
//.addProperty( API.label, "hasTransactionRecord" )
//;
//maps
//.createResource( "http://landregistry.data.gov.uk/def/ppi/transactionId" )
//.addProperty( API.label, "transactionId" )
//;
//maps
//.createResource( "http://landregistry.data.gov.uk/def/ppi/TransactionIdDatatype" )
//.addProperty( API.label, "ppi_TransactionIdDatatype" )
//;
////maps
////.createResource( "http://landregistry.data.gov.uk/def/ppi/hasTransactionRecord" )
////.addProperty( API.label, "hasTransactionRecord" )
////;
////maps
////.createResource( "http://landregistry.data.gov.uk/def/ppi/hasTransactionRecord" )
////.addProperty( API.label, "hasTransactionRecord" )
////;
////maps
////.createResource( "http://landregistry.data.gov.uk/def/ppi/hasTransactionRecord" )
////.addProperty( API.label, "hasTransactionRecord" )
////;
////maps
////.createResource( "http://landregistry.data.gov.uk/def/ppi/hasTransactionRecord" )
////.addProperty( API.label, "hasTransactionRecord" )
////;
//
//StandardShortnameService sns = new StandardShortnameService( maps );
//
//NameMap nameMap = sns.nameMap(); // new NameMap();
////nameMap.load( pm, maps );
////
//EndpointMetadata.addAllMetadata
//( mm
//, ru
//, uriForDefinition
//, new Bindings()
//, nameMap
//, true
//, thisMetaPage
//, 0
//, 10
//, true
//, resultList
//, setsMeta
//, wantsMeta
//, "select query"
//, "view query"
//, new SparqlSource( null, "sparql:endpoint", null )
//, CollectionUtils.set( "all", "basic", "description", "none" )
//, formats
//, details
//); 
//		
//
