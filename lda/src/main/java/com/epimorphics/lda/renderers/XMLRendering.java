/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.shortnames.ShortnameService;
//import com.epimorphics.lda.support.CycleFinder;
import com.epimorphics.vocabs.API;

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
	private final Map<String, String> nameMap;
	private final boolean suppressIPTO;
	
	private Resource itemsResource = null;
	
	private final Set<Resource> dontExpand = new HashSet<Resource>();
	
	/** if true, property values will appear in sorted order */
	private final boolean sortPropertyValues = true;
	
	public XMLRendering( Model m, ShortnameService sns, boolean suppressIPTO, Document d ) {
		this.d = d;
		this.sns = sns;
		this.suppressIPTO = suppressIPTO;
		this.nameMap = sns.nameMap().stage2().loadPredicates(m, m).result();
	}

	/** 
		The way in -- all external uses come via this method, so we can
		compute useful things before doing the actual rendering.
		
		NOTE. This code was written hastily and will be pruned and
		simplified as soon as possible, but not today.	
	*/
	public Element addResourceToElement( Element e, Resource x, MergedModels mm ) {
				
		itemsResource = getItemsResource( x );
		Set<RDFNode> selectedItems = getItemsList( itemsResource );
				
		for (RDFNode m: selectedItems) dontExpand.add( m.asResource() );
		
		Model objectModel = mm.getObjectModel();
		
		Resource xInObjectModel = x.inModel(objectModel );
		
		Resource xInMetaModel = x.inModel(mm.getMetaModel() );
	//
	// This is the top-level expansion: we know which properties are meta-data
	// and which are not. We expand the items separately from the rest of the
	// properties.	
	//
		addIdentification( e, x );
		// dontExpand.add( x );
	//
		List<Property> metaProperties = asSortedList( xInMetaModel.listProperties().mapWith( Statement.Util.getPredicate ).toSet() );
	//
		// if (suppressIPTO) properties.remove( FOAF.isPrimaryTopicOf );
		Trail t2 = new Trail();
		t2.see(xInMetaModel);
	//	
	//
		for (Property p: metaProperties)			
			addPropertyValues( t2, e, xInMetaModel, p, false );
	//
		boolean hasPrimaryTopic = xInMetaModel.hasProperty( FOAF.primaryTopic );
		
		Trail t = new Trail();
		if (hasPrimaryTopic) { 			
			Element pt = findByNodeName( e, "primaryTopic" );
			String itemUri = pt.getAttribute( "href" );
			Resource item = objectModel.createResource( itemUri );
			List<Property> itemProperties = asSortedList( item.listProperties().mapWith( Statement.Util.getPredicate ).toSet() );
			for (Property ip: itemProperties) {
				addPropertyValues( t, pt, item, ip, true );
			}			
		} else {			
			NodeList nl = findItems( e ).getChildNodes();
			for (int i = 0; i < nl.getLength(); i += 1) {
				Element anItem = (Element) nl.item(i);
				String itemUri = anItem.getAttribute( "href" );
				Resource item = objectModel.createResource( itemUri );
				List<Property> itemProperties = asSortedList( item.listProperties().mapWith( Statement.Util.getPredicate ).toSet() );
				for (Property ip: itemProperties) {
					addPropertyValues( t, anItem, item, ip, true );
				}
			}
		}
	//
		return e;
	}
	
	private Element findItems( Element e ) {
		return findByNodeName( e, "items" );
	}
	
	private Element findByNodeName( Element e, String name ) {
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i += 1) {
			Node it = nl.item(i);
			if (it.getNodeName().equals( name ))
				return (Element) it;
		}
		System.err.println( ">> could not find " + name );
		return null;
	}

	// Answer a List of all the items hanging off the RDF list
	// itemsResource, or an empty list if itemsResource is null.
	private Set<RDFNode> getItemsList( Resource itemsResource ) {
		return itemsResource == null 
			? new HashSet<RDFNode>() 
			: new HashSet<RDFNode>( RDFUtil.asJavaList( itemsResource ) )
			;
	}

	// Answer the resource I that is the value of an api:items property
	// of x, if there is one, and null otherwise. If x is the root
	// of a resultset graph, I is the list of selected items.
	private Resource getItemsResource( Resource x ) {
		StmtIterator sit = x.listProperties( API.items );
		return sit.hasNext() ? sit.next().getResource() : null;
	}
	
	static class Trail {
		
		final Set<Resource> seen = new HashSet<Resource>();
		
		boolean unseen( Resource x ) {
			return !seen.contains( x );
		}
		
		void see( Resource x ) {
			seen.add( x );
		}
		
		void unsee( Resource x ) {
			seen.remove( x );
		}		
		
		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < seen.size(); i += 1) sb.append( "  " );
			for (Resource x: seen) sb.append( " " ).append( nice( x ) );
			return sb.toString();
		}
		
		public String nice( Resource x ) {
			if (x.isAnon()) return "__";
			String s = x.getModel().shortForm( x.getURI() );
			return s == null || s.equals(x.getURI())? x.getLocalName() : s;
		}
	}
	
	/**
	    Add a resource <code>x</code> to the DOM element <code>e</code>.
	    
	*/
	
	private Element elementAddResource( Trail t, Element e, Resource x, boolean expandRegardless ) {
		addIdentification( e, x );
								
		if (t.unseen( x ) || expandRegardless) {
			t.see(x);
			List<Property> properties = asSortedList( x.listProperties().mapWith( Statement.Util.getPredicate ).toSet() );
			// if (suppressIPTO) properties.remove( FOAF.isPrimaryTopicOf );
			for (Property p: properties) addPropertyValues( t, e, x, p, false );		
			t.unsee( x );
		}		
		
		return e;
	}

	private void addIdentification( Element e, Resource x ) {
		if (x.isURIResource())  
			e.setAttribute( "href", x.getURI() );
		else if (dontExpand.contains( x )) {
			e.setAttribute( "ref", idFor( e, x ) );
		} else {
			e.setAttribute( "id", idFor( e, x ) );
		}
	}

	/**
	    Attach a value to a property element.
	*/
	private Element giveValueToElement( Trail t, Element pe, RDFNode v, boolean expandRegardless ) {
		if (v.isLiteral()) {
			addLiteralToElement( pe, (Literal) v );
		} else {
			Resource r = v.asResource();
			if (inPlace( r )) {
				addIdentification( pe, r );
				if (expandRegardless) elementAddResource( t, pe, r, expandRegardless );
			} else if (RDFUtil.isList( r )) {
				for (RDFNode item: RDFUtil.asJavaList( r ) ) 
					appendValueAsItem( t, pe, item, r.equals(itemsResource) );
			} else if (r.listProperties().hasNext()) 
				elementAddResource( t, pe, r, expandRegardless );
			else if (v.isAnon()) {
				if (needsId( v )) pe.setAttribute( "id", idFor( pe, r ) );
			} else {
				pe.setAttribute( "href", r.getURI() );
			}
		}
		return pe;
	}

	private List<Property> asSortedList( Set<Property> set ) {
		List<Property> properties = new ArrayList<Property>( set );
		Collections.sort( properties, new Comparator<Property>() {
            @Override public int compare(Property a, Property b) {
            	
//            	System.err.println( ">> a " + a.getURI() );
//            	String x = nameMap.get( a.getURI() );
//            	if (x == null) {
//            		x = a.getLocalName();
//            		System.err.println( ">> " + a.getURI() + " had to be forced to " + x );
//            	}
//
////            	System.err.println( ">> b " + b.getURI() );
//            	String y = nameMap.get( b.getURI() );
//            	if (y == null) y = a.getLocalName();
//            	
//            	if (true) return x.compareTo( y );
            	
                return nameMap.get( a.getURI() ).compareTo( nameMap.get( b.getURI() ) );
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
		String shorter = nameMap.get( r.getURI() );
		return shorter == null ? r.getLocalName() : shorter;
	}

	private void addPropertyValues( Trail t, Element e, Resource x, Property p, boolean expandRegardless ) {
//		 System.err.println( ">> add property values for " + p );
//		System.err.println( ">> addPropertyValues for " + shortNameFor(p) );
		
//		System.err.println( ">> short name is " + shortNameFor( p ) + " for " + p );
		
		Element pe = d.createElement( shortNameFor( p ) );
		// System.err.println( ">> pe := " + pe );
		e.appendChild( pe );
		// System.err.println( ">> e := " + e );
		Set<RDFNode> values = x.listProperties( p ).mapWith( Statement.Util.getObject ).toSet();		
		
		if (values.size() > 1 || isMultiValued( p )) {
			for (RDFNode value: sortObjects( values )) appendValueAsItem(t, pe, value, expandRegardless);
		} else if (values.size() == 1) {
			giveValueToElement( t, pe, values.iterator().next(), expandRegardless );
		}
	}

	private void appendValueAsItem( Trail t, Element pe, RDFNode value, boolean expandRegardless ) {
		Element item = d.createElement( "item" );
		giveValueToElement( t, item, value, expandRegardless );
		pe.appendChild( item );
	}

	private boolean inPlace( Resource r ) {
		if (r.isAnon()) return false;
		if (dontExpand.contains( r )) return true;
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

	private boolean needsId( RDFNode v ) {
		return false;
	}

	private boolean isMultiValued( Property p ) {
		if (p.equals( RDF.type )) return true;
		ContextPropertyInfo px = sns.asContext().getPropertyByURI(p.getURI());
		return px != null && px.isMultivalued();
	}
	
	private String shortNameFor( Resource r ) {
		return shortNameFor( r.getURI() );
	}
	
	private String shortNameFor( String URI ) {
		String s = nameMap.get( URI );
		if (s == null) s = URI.replaceFirst( ".*[#/]",  "" );
		return s;
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