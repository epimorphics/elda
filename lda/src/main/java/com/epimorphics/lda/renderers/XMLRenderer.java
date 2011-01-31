/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.epimorphics.lda.core.APIResultSet;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
From the spec: 

The XML formatter creates an XML representation that is very similar to the 
JSON representation. The outermost object is a <result> element with 
format and version attributes.

The resource described in the <result> element is the entry point 
into the graph, as described above (the item for an item endpoint, 
the page for a list endpoint).

Resources are mapped onto XML elements as follows:

    * if the resource is a blank node that is the object or more 
    	than one statement within the graph, the element is given a id 
    	attribute that contains a unique identifier for that blank node
    	
    * otherwise, if the resource is not a blank node, 
    	the element is given an href attribute that contains the 
    	URI of the resource 

The RDF properties of a resource are mapped onto XML elements. The 
name of the XML element is:

    * the short name for the property, as described in the property paths section, if it has one
    
    * the rdfs:label of the property, if it is a legal short name for a property 
    	that doesn't clash with an existing name
     
    * the local name of the property (the part after the last hash or slash), 
    	if it is a legal short name for a property that doesn't clash with an existing name
     
    * the prefix associated with the namespace of the property (the part
     	before the last hash or slash), concatenated with an underscore, 
     	concatenated with the local name of the property 

The contents of the XML element is a sequence of <item> elements if the RDF 
property has more than one value in the RDF graph or if the api:multiValued 
property of the RDF property has the value true.

Each RDF value is mapped onto some XML content as follows:

    * if the value is a literal, it is mapped to a text node holding the 
    	value itself; lang or datatype attributes on the element hold the
    	language code and the short name of the datatype as applicable
    
    * otherwise, if the value is a rdf:List, it is mapped to a sequence 
      of <item> elements, one representing each of the results of mapping 
      the members of the list to XML
    
    * otherwise, if the value is a resource which is the subject of 
    	a statement in the RDF graph, it is mapped onto an XML element
    	as described here
    
    * otherwise, if the value is a blank node with no properties 
    	it is mapped onto an empty XML element (with an id attribute
    	if it it referenced more than once)
    
    * otherwise, if the value is a resource the element is given an
    	href attribute whose value is the URI of the resource 


*/
public class XMLRenderer implements Renderer {

	public static final String XML_MIME = "text/xml";
	
	public XMLRenderer() {
	}
	
	@Override public String getMimeType() {
		return XML_MIME;
	}

	@Override public synchronized String render( APIResultSet results ) {
			Document d = getBuilder().newDocument();
			Resource root = results.getRoot();
			Rendering r = new Rendering( d );
			Element e = r.traverse( root );
			Element bloop = d.createElement( "BLOOP" );
			d.appendChild( bloop );
			bloop.appendChild( e );
			StringWriter sw = docToString( d );
		return sw.toString();
	}

	private void printDocument( Document d ) {
		try {
			StringWriter sw = docToString(d);
			System.err.println( ">> " + sw.toString() );
		} catch (Throwable t) {
			throw new RuntimeException( t );
		} 
	}

	private StringWriter docToString( Document d ) {
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty( OutputKeys.INDENT, "yes" );
			t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource ds = new DOMSource( d );
			StringWriter sw = new StringWriter();
			StreamResult sr = new StreamResult( sw );
			t.transform( ds, sr );
			return sw;
		} catch (Throwable t) {
			throw new RuntimeException( t );
		} 
	}	
	
	private static DocumentBuilder getBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException( e );
		}
	}

	static class Rendering {
		
		private final Document d;
		
		public Rendering( Document d ) {
			this.d = d;
		}
		
		private final Set<Resource> seen = new HashSet<Resource>();
	
		private Element traverse( Resource x ) {
			// System.err.println( "traverse: " + x );
			Element e = elementFor( x );
			if (seen.add( x )) {
				Set<Property> properties = x.listProperties().mapWith( Statement.Util.getPredicate ).toSet();
				for (Property p: properties) addPropertyValues( e, x, p );
			}
			return e;
		}
	
		private void addPropertyValues( Element e, Resource x, Property p ) {
			// System.err.println( ">> add property values for " + p );
			Element pe = d.createElement( shortNameFor( p ) );
			// System.err.println( ">> pe := " + pe );
			e.appendChild( pe );
			// System.err.println( ">> e := " + e );
			Set<RDFNode> values = x.listProperties( p ).mapWith( Statement.Util.getObject ).toSet();
			if (values.size() > 1 || isMultiValued( p )) {
				for (RDFNode value: values) {
					Element i = d.createElement( "item" );
					addValueToProperty( i, value );
					// i.appendChild( elementForValue( value ) );
					pe.appendChild( i );
				}
			} else if (values.size() == 1) {
				// pe.appendChild( elementForValue( values.iterator().next() ) );
				addValueToProperty( pe, values.iterator().next() );
			}
		}

		public void addValueToProperty( Element pe, RDFNode v ) {
			if (v.isLiteral()) {
				addLiteralToElement( pe, (Literal) v );
			} else {
				Resource r = v.asResource();
				if (inPlace( r )) pe.setAttribute( "href", r.getURI() );
				else if (isRDFList( r )) addItems( pe, r.as(RDFList.class).asJavaList() );
				else pe.appendChild( elementForValue( v ) );
			}
		}

		private void addItems( Element pe, List<RDFNode> jl ) {
			Element items = pe.getOwnerDocument().createElement( "items" );
			for (RDFNode item: jl) items.appendChild( elementForValue( item ) );
			pe.appendChild( items );			
		}

		private boolean inPlace( Resource r ) {
			if (r.isAnon()) return false;
			if (r.listProperties().hasNext()) return false;
			return true;
		}

		private void addLiteralToElement( Element e, Literal L ) {
			String lang = L.getLanguage();
			if (lang.length() > 0) e.setAttribute( "lang", lang );
			String type = L.getDatatypeURI();
			if (type != null) e.setAttribute( "datatype", shortNameFor( type ) );
			e.appendChild( d.createTextNode( L.getLexicalForm() ) );
		}

		private Element elementForValue( RDFNode v ) {
			Element e = d.createElement( "item" );
			if (v.isLiteral()) {
				addLiteralToElement( e, (Literal) v );
			} else if (isRDFList( v )){
				List<RDFNode> items = v.as(RDFList.class).asJavaList();
				for (RDFNode item: items) {
					addValueToProperty( e, item );
				}
			} else if (v.isResource() && v.asResource().listProperties().hasNext()){
				return traverse( v.asResource() );
			} else if (v.isAnon() && !v.asResource().listProperties().hasNext()) {
				if (needsId( v )) e.setAttribute( "id", idFor( v.asResource() ));
			} else {
				e.setAttribute( "href", v.asResource().getURI() );
			}
	    return e;
		}
	
		private boolean needsId( RDFNode v ) {
			return false;
		}

		private boolean isRDFList(RDFNode v) {
			return v.isResource() && v.asResource().hasProperty( RDF.first );
		}

		private boolean isMultiValued( Property p ) {
			return false;
		}
	
	
		final Map<String, String> shortNames = new HashMap<String, String>();
		
		private String shortNameFor( Property p ) {
			return shortNameFor( p.getURI() );
		}
		
		private String shortNameFor( String URI ) {
			String s = shortNames.get( URI );
			if (s == null) shortNames.put( URI, s = createShortName( URI ) );
			return s;
		}
	
		private String createShortName( String URI ) {
			// fx:cough.
			return ResourceFactory.createResource( URI ).getLocalName();
		}
	
		private Element elementFor( Resource x ) {
			Element e = d.createElement( "item" );
			if  (x.isAnon()) e.setAttribute( "id", idFor( x ) );
			else e.setAttribute( "href", x.getURI() );
			return e;
		}
	
		final Map<AnonId, String> idMap = new HashMap<AnonId, String>();
	
		private String idFor( Resource x ) {
			String id = idMap.get(x.getId());
			if (id == null) idMap.put(x.getId(), id = newId() );
			return id;
		}
	
		int idCount = 1000;
		
		private String newId() {
			return "anon_" + ++idCount;
		}

	}
}
