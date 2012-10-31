package com.epimorphics.lda.renderers.velocity;

import java.io.PrintStream;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;

public class WrappedNode {
	
	final Resource r;
	final String label;
	final RDFNode basis;
	final List<Literal> labels;
	final ShortNames sn;
	
	public int hashCode() {
		return basis.hashCode();
	}
	
	public boolean equals( Object other ) {
		return other instanceof WrappedNode && basis.equals( ((WrappedNode) other).basis );
	}
	
	public WrappedNode( ShortNames sn, RDFNode r ) {
		this.sn = sn;
		this.r = (r.isResource() ? r.asResource() : null);
		this.label = "LIT";
		this.basis = r;
		this.labels = this.r == null ? new ArrayList<Literal>() : Help.labelsFor( this.r );
	}
	
	public WrappedNode( ShortNames sn, Resource r ) {
		this.sn = sn;
		this.basis = r;
		this.r = r;
		this.label = Help.labelFor( r );
		this.labels = Help.labelsFor( r );
	}
	
	public String getLabel() {
		return getLabel( "" );
	}
	
	/**
	    Answer the lexical form of some label of this wrapped 
	    resource which has <code>wantLanguage</code>. If there isn't
	    one, answer some lexical form with no language. If there
	    isn't one, answer the local name of the resource with any
	    _s replaced by spaces.
	*/
	public String getLabel( String wantLanguage ) {
		Literal plain = null;
		for (Literal l: labels) {
			String thisLanguage = l.getLanguage();
			if (thisLanguage.equals(wantLanguage)) return l.getLexicalForm();
			if (thisLanguage.equals("")) plain = l;
		}
		return plain == null ? r.getLocalName().replaceAll("_", " ") : plain.getLexicalForm();
	}
	
	public String getId( Map<Resource, String> ids ) {
		String id = ids.get( r );
		if (id == null) ids.put( r,  id = "ID-" + (ids.size() + 10000) );
		return id;
	}
	
	public String shortForm() {
		if (r == null) return shortLiteral();
		return shortURI();
	}
	
	private String shortURI() {
		return sn.get(r);
	}

	private String shortLiteral() {
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
	
	public String getLiteralType() {
		return sn.get( basis.asLiteral().getDatatypeURI() );
	}
	
	public boolean isList() {
		return basis.isAnon() && basis.asResource().canAs( RDFList.class );
	}
	
	public List<WrappedNode> asList() {
        List<RDFNode> rawlist = basis.as( RDFList.class ).asJavaList();
        List<WrappedNode> result = new ArrayList<WrappedNode>( rawlist.size() );
        for (RDFNode n : rawlist) result.add( new WrappedNode( sn, n ) );
        return result;
	}
	
	public List<WrappedNode> getValues( WrappedNode p ) {
		List<WrappedNode> result = propertyValues.get(p);
		return result;
	}

	public List<WrappedNode> getProperties() {
		Set<WrappedNode> properties = new HashSet<WrappedNode>();
		for (WrappedNode wp: propertyValues.keySet()) properties.add( wp );
		ArrayList<WrappedNode> result = new ArrayList<WrappedNode>( properties );
		return result;
	}

	public Resource asResource() {
		return r;
	}
	
	final Map<WrappedNode, List<WrappedNode>> propertyValues = 
		new HashMap<WrappedNode, List<WrappedNode>>();
	
	public void addPropertyValue( WrappedNode wp, WrappedNode o) {
		List<WrappedNode> values = propertyValues.get( wp );
		if (values == null)
			propertyValues.put( wp, values = new ArrayList<WrappedNode>() );
		values.add( o );
	}

	public void debugShow(PrintStream ps) {
		if (isLiteral()) {
			ps.print( " '" + basis.asLiteral().getLexicalForm() + "'" );
		} else {
			ps.print( "<<" + getLabel() + ">>" );
			ps.print( " (" );
			String and = "";
			for (WrappedNode wp: propertyValues.keySet()) {
				for (WrappedNode w: propertyValues.get( wp )) {					
					ps.print( and ); and = "; ";
					ps.print( wp.r.getLocalName() );
					ps.print( " " );
					w.debugShow( ps );
				}
			}
			ps.print( ")" );
		}
	}		
}