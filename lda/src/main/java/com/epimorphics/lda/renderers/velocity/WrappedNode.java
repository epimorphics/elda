package com.epimorphics.lda.renderers.velocity;

import java.io.PrintStream;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class WrappedNode {
	
	final Resource r;
	final String label;
	final RDFNode basis;
	
	public int hashCode() {
		return basis.hashCode();
	}
	
	public boolean equals( Object other ) {
		return other instanceof WrappedNode && r.equals( ((WrappedNode) other).r );
	}
	
	public WrappedNode( RDFNode r ) {
		this.r = (r.isResource() ? r.asResource() : null);
		this.label = "LIT";
		this.basis = r;
	}
	
	public WrappedNode( Resource r ) {
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
	
	public String toString() {
		if (basis.isLiteral()) return basis.asLiteral().getLexicalForm();
		return basis.asResource().getLocalName();
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
	
	public boolean isLiteral() {
		return basis.isLiteral();
	}
	
	public boolean isResource() {
		return basis.isResource();
	}
	
	public boolean isAnon() {
		return basis.isAnon();
	}
	
	public String getLanguage() {
		return basis.asLiteral().getLanguage();
	}
	
	public String getType( Map<Resource, String> shortNames ) {
		return basis.asLiteral().getDatatypeURI();
	}
	
	public boolean isList() {
		return basis.isAnon() && basis.asResource().canAs( RDFList.class );
	}
	
	public List<WrappedNode> asList() {
        List<RDFNode> rawlist = basis.as( RDFList.class ).asJavaList();
        List<WrappedNode> result = new ArrayList<WrappedNode>( rawlist.size() );
        for (RDFNode n : rawlist) result.add( new WrappedNode( n ) );
        return result;
	}
	
	public List<WrappedNode> getValues( WrappedNode subject ) {
		List<WrappedNode> values = new ArrayList<WrappedNode>();
		for (Statement s: subject.r.listProperties( asProperty(r) ).toList()) {
			values.add( new WrappedNode( s.getObject() ) );
		}
		return values;
	}
	
	private Property asProperty(Resource r) {
		return r.getModel().createProperty( r.getURI() );
	}

	public List<WrappedNode> getProperties() {
		Set<WrappedNode> properties = new HashSet<WrappedNode>();
		for (Statement s: r.listProperties().toList()) properties.add( new WrappedNode(s.getPredicate()) );
		return new ArrayList<WrappedNode>( properties );
	}

	public Resource asResource() {
		return r;
	}
	
	public static class PropertyValue {
	
		final WrappedNode p;
		final WrappedNode o;
		
		public PropertyValue( WrappedNode p, WrappedNode o ) {
			this.p = p; this.o = o;
		}
	}
	
	final List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();

	public void addPropertyValue(Property p, WrappedNode o) {
		propertyValues.add( new PropertyValue( new WrappedNode( p ), o ) );
	}

	public void debugShow(PrintStream ps) {
		if (isLiteral()) {
			ps.print( " '" + basis.asLiteral().getLexicalForm() + "'" );
		} else {
			ps.print( r.getURI() );
			ps.print( " (" );
			String and = "";
			for (PropertyValue pv: propertyValues) {
				ps.print( and ); and = "; ";
				ps.print( pv.p.getURI() );
				ps.print( " " );
				pv.o.debugShow( ps );
			}
			ps.print( ")" );
		}
	}		
}