/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDF;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.MultiMap;

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

    * the short name for the property, as described in the property paths section, 
      if it has one
    
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
public class XMLRendering {
	
	private final Document d;
	private final ShortnameService sns;
	private final MultiMap<String, String> nameMap;
	private final boolean suppressIPTO;
	
	/** if true, property values will appear in sorted order */
	private final boolean sortPropertyValues = true;
	
	public XMLRendering( Model m, ShortnameService sns, boolean stripHas, boolean suppressIPTO, Document d ) {
		this.d = d;
		this.sns = sns;
		this.suppressIPTO = suppressIPTO;
		this.nameMap = sns.nameMap().stage2(stripHas).load(m, m).result();
	}
	
	private final Set<Resource> seen = new HashSet<Resource>();

	Element addResourceToElement( Element e, Resource x ) {
		addIdentification( e, x );
		if (seen.add( x )) {
			List<Property> properties = asSortedList( x.listProperties().mapWith( Statement.Util.getPredicate ).toSet() );
			if (suppressIPTO) properties.remove( FOAF.isPrimaryTopicOf );
			for (Property p: properties) addPropertyValues( e, x, p );
			seen.remove( x );
		}
		return e;
	}

	private List<Property> asSortedList( Set<Property> set ) {
		List<Property> properties = new ArrayList<Property>( set );
		Collections.sort( properties, new Comparator<Property>() {
            @Override public int compare(Property a, Property b) {
                return nameMap.getOne( a.getURI() ).compareTo( nameMap.getOne( b.getURI() ) );
            }
        	} );
		return properties;
	}
	
	private List<RDFNode> sortObjects( Set<RDFNode> objects ) {
		List<RDFNode> result = new ArrayList<RDFNode>( objects );
		if (sortPropertyValues)
			Collections.sort( result, new Comparator<RDFNode>() {
	            @Override public int compare( RDFNode a, RDFNode b ) {
	                return spelling( a ).compareTo( spelling( b ) );
	            }
	        	} );
		return result;
	}

	protected String spelling( RDFNode n ) {
		if (n.isURIResource()) return resourceSpelling( (Resource) n );
		if (n.isLiteral()) return ((Literal) n).getLexicalForm();
		return ((Resource) n).getId().toString();
	}

	private String resourceSpelling( Resource r ) {
		String shorter = nameMap.getOne( r.getURI() );
		return shorter == null ? r.getLocalName() : shorter;
	}

	private void addIdentification( Element e, Resource x ) {
		if (x.isURIResource())  
			e.setAttribute( "href", x.getURI() );
		else if (seen.contains( x )) {
			e.setAttribute( "ref", idFor( e, x ) );
		} else {
			e.setAttribute( "id", idFor( e, x ) );
		}
	}

	private void addPropertyValues( Element e, Resource x, Property p ) {
		// System.err.println( ">> add property values for " + p );
		Element pe = d.createElement( shortNameFor( p ) );
		// System.err.println( ">> pe := " + pe );
		e.appendChild( pe );
		// System.err.println( ">> e := " + e );
		Set<RDFNode> values = x.listProperties( p ).mapWith( Statement.Util.getObject ).toSet();
		if (values.size() > 1 || isMultiValued( p )) {
			for (RDFNode value: sortObjects( values )) appendValueAsItem(pe, value);
		} else if (values.size() == 1) {
			giveValueToElement( pe, values.iterator().next() );
		}
	}

	private void appendValueAsItem( Element pe, RDFNode value ) {
		Element item = d.createElement( "item" );
		elementForValue( item, value );
		pe.appendChild( item );
	}

	public void giveValueToElement( Element pe, RDFNode v ) {
		if (v.isLiteral()) {
			addLiteralToElement( pe, (Literal) v );
		} else {
			Resource r = v.asResource();
			if (inPlace( r ))
				addIdentification(pe, r);
			else if (RDFUtil.isRDFList( r )) 
				addItems( pe, r.as(RDFList.class).asJavaList() );
			else 
				elementForValue( pe, v );
		}
	}

	private void addItems( Element pe, List<RDFNode> jl ) {	
		for (RDFNode item: jl) appendValueAsItem( pe, item );
	}

	private boolean inPlace( Resource r ) {
		if (r.isAnon()) return false;
		if (seen.contains( r )) return true;
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

	private Element elementForValue( Element e, RDFNode v ) {
		if (v.isLiteral()) {
			addLiteralToElement( e, (Literal) v );
		} else if (RDFUtil.isRDFList( v )){
			List<RDFNode> items = v.as(RDFList.class).asJavaList();
			for (RDFNode item: items) {
				giveValueToElement( e, item );
			}
		} else if (v.isResource() && v.asResource().listProperties().hasNext()){
			return addResourceToElement( e, v.asResource() );
		} else if (v.isAnon() && !v.asResource().listProperties().hasNext()) {
			if (needsId( v )) e.setAttribute( "id", idFor( e, v.asResource() ));
		} else {
			e.setAttribute( "href", v.asResource().getURI() );
		}
    return e;
	}

	private boolean needsId( RDFNode v ) {
		return false;
	}

	private boolean isMultiValued( Property p ) {
		if (p.equals( RDF.type )) return true; // HACKERY
		ContextPropertyInfo px = sns.asContext().getPropertyByURI(p.getURI());
		return px != null && px.isMultivalued();
	}
	
	private String shortNameFor( Resource r ) {
		return shortNameFor( r.getURI() );
	}
	
	private String shortNameFor( String URI ) {
		return nameMap.getOne( URI );
	}

	final Map<AnonId, String> idMap = new HashMap<AnonId, String>();

	private String idFor( Element e, Resource x ) {
		String id = idMap.get(x.getId());
		if (id == null) idMap.put(x.getId(), id = newId(e.getTagName()) );
		return id;
	}

	int idCount = 1000;
	
	private String newId(String name) {
		return "_:" + name + "-" + ++idCount;
	}

}