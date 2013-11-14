package com.epimorphics.lda.xml_rendering_regression.tests;

import java.io.*;
import java.util.Map;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.renderers.XMLRenderer;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.util.DOMUtils;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestXMLRendererWithGold extends XMLTestCase {
	
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
		GoldXMLTestHelp b = GoldXMLTestHelp.load( goldName );
	//	
		final MergedModels mm = new MergedModels( b.objectModel );
		mm.getMetaModel().add( b.metaModel );
		Resource root = mm.getMetaModel().createResource( b.root_uri );
		Document d = DOMUtils.newDocument();
	//
		XMLRenderer r = new XMLRenderer( b.sns );
		
		Map<String, String> termBindings =
			new CompleteContext(CompleteContext.Mode.PreferPrefixes, b.sns.asContext(), mm.getMergedModel() )
			.Do( mm.getMergedModel(), mm.getMergedModel() )
			;
		
		r.renderInto( root, mm, d, termBindings );
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

