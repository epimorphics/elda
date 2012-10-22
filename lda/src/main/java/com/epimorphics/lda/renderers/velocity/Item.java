package com.epimorphics.lda.renderers.velocity;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class Item {
	
	final Resource r;
	final String label;
	final RDFNode basis;
	
	public int hashCode() {
		return r.hashCode();
	}
	
	public boolean equals( Object other ) {
		return other instanceof Item && r.equals( ((Item) other).r );
	}
	
	public Item( RDFNode r ) {
		this.r = (r.isResource() ? r.asResource() : null);
		this.label = "LIT";
		this.basis = r;
	}
	
	public Item( Resource r ) {
		this.basis = r;
		this.r = r;
		this.label = Help.labelFor( r );
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getId( Map<Resource, String> ids ) {
		String id = ids.get( r );
		if (id == null) ids.put( r,  id = "ID-" + (ids.size() + 10000) );
		return id;
	}
	
	public String shortForm( Map<Resource, String> shortNames ) {
		if (r == null) return shortLiteral( shortNames );
		return shortURI( shortNames );
	}
	
	private String shortURI(Map<Resource, String> shortNames) {
		String sn = shortNames.get(r);
		return sn == null ? r.getLocalName() : sn;
	}

	private String shortLiteral(Map<Resource, String> shortNames) {
		return basis.asLiteral().getLexicalForm();
	}

	public String getObjectString() {
		if (r == null) {
			return basis.asLiteral().getLexicalForm();
		}
		return r.getURI();
	}
	
	public String getURI() {
		return r.getURI();
	}
	
	public List<Item> getValues( Item subject ) {
		List<Item> values = new ArrayList<Item>();
		for (Statement s: subject.r.listProperties( asProperty(r) ).toList()) {
			values.add( new Item( s.getObject() ) );
		}
		return values;
	}
	
	private Property asProperty(Resource r) {
		return r.getModel().createProperty( r.getURI() );
	}

	public List<Item> getProperties() {
		Set<Item> properties = new HashSet<Item>();
		for (Statement s: r.listProperties().toList()) properties.add( new Item(s.getPredicate()) );
		return new ArrayList<Item>( properties );
	}		
}