package com.epimorphics.lda.renderers.velocity;

import java.io.PrintStream;
import java.net.URI;
import java.util.*;

import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    A WrappedNode is an RDF node wrapped in a shell of useful methods to be
    called from Velocity templates.
*/
public class WrappedNode {
	
	final Resource r;
	final String label;
	final RDFNode basis;
	final List<Literal> labels;
	
	final Bundle bundle;

	// created on demand
	protected List<WrappedNode> properties = null;
	protected List<WrappedNode> inverses = null;
	
	/**
	    Struct holding the short-names and id maps.
	*/
	public static class Bundle {
		final ShortNames sn;
		final IdMap ids;
		
		public Bundle(ShortNames sn, IdMap ids) {
			this.sn = sn;
			this.ids = ids;
		}
	}

	public static List<WrappedNode> itemise(Bundle b, List<Resource> items) {
		List<WrappedNode> result = new ArrayList<WrappedNode>( items.size() );;
		for (Resource i: items) result.add( new WrappedNode(b, i) );
		return result;
	}
	
	static final List<Literal> noLabels = new ArrayList<Literal>();
	
	public int hashCode() {
		return basis.hashCode();
	}
	
	public boolean equals( Object other ) {
		return other instanceof WrappedNode && basis.equals( ((WrappedNode) other).basis );
	}
	
	public WrappedNode( ShortNames sn, IdMap ids, RDFNode r ) {
		// TODO may be obsolete
		this( new Bundle( sn, ids ), r );
	}
	
	public WrappedNode( Bundle b, RDFNode r ) {
		this.bundle = b;
		this.r = (r.isResource() ? r.asResource() : null);
		this.label = "LIT";
		this.basis = r;
		this.labels = this.r == null ? noLabels : Help.labelsFor( this.r );
	}
	
	public WrappedNode( Bundle b, Resource r ) {
		this.bundle = b;
		this.basis = r;
		this.r = r;
		this.label = Help.labelFor( r );
		this.labels = Help.labelsFor( r );
	}
	
	public WrappedString getLabel() {
		return new WrappedString( label );
	}
	
	/**
	    Return the lexical form of some label of this wrapped 
	    resource which has <code>wantLanguage</code>. If there isn't
	    one, return some lexical form with no language. If there
	    isn't one, return the local name of the resource with any
	    _s replaced by spaces.
	*/
	public WrappedString getLabel( String wantLanguage ) {
		if (wantLanguage.equals("")) return getLabel();
	//
		Literal plain = null;
		for (Literal l: labels) {
			String thisLanguage = l.getLanguage();
			if (thisLanguage.equals(wantLanguage)) return new WrappedString( l.getLexicalForm() );
			if (thisLanguage.equals("")) plain = l;
		}
		String raw = plain == null ? r.getLocalName().replaceAll("_", " ") : plain.getLexicalForm();
		return new WrappedString(raw);
	}
	
	/**
	    Return the ID of this WrappedNode by appealing to the shared IdMap.
	 */
	public String getId() {
		return bundle.ids.get(r);
	}
	
	/**
	     True iff this WrappedNode is a Resource with just one label and no 
	     other properties.
	*/
	public boolean isJustALabel() {
		if (r == null) return false;
		List<Statement> properties = r.listProperties().toList();
		return properties.size() == 1 && properties.get(0).getPredicate().equals(RDFS.label);
	}
	
	public WrappedString shortForm() {
		if (r == null) return shortLiteral();
		return shortURI();
	}
	
	private WrappedString shortURI() {
		return new WrappedString( bundle.sn.getWithUpdate(r) );
	}

	private WrappedString shortLiteral() {
		return new WrappedString( basis.asLiteral().getLexicalForm() );
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
	
	public WrappedNode change( String prefix, WrappedNode p, WrappedNode v ) {
		URI ru = URIUtils.newURI( r.getURI() );
		String key = prefix + p.shortForm().content;
		String value = v.toString();
		URI u = URIUtils.replaceQueryParam( ru, key, value );	
	//
		Resource changed = r.getModel().createResource( u.toString() );
		return new WrappedNode( bundle, changed );
	}
	
	public WrappedString getURI() {
		return new WrappedString( r.getURI() );
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
	
	public WrappedString getLiteralType() {
		return new WrappedString( bundle.sn.getWithUpdate( basis.asLiteral().getDatatypeURI() ) );
	}
	
	/**
	    Return the value of the wrapped literal
	*/
	public Object getLiteralValue() {
		return basis.asLiteral().getValue();
	}
	
	public boolean isList() {
		return basis.isAnon() && basis.asResource().canAs( RDFList.class );
	}
	
	public List<WrappedNode> asList() {
        List<RDFNode> rawlist = basis.as( RDFList.class ).asJavaList();
        List<WrappedNode> result = new ArrayList<WrappedNode>( rawlist.size() );
        for (RDFNode n : rawlist) result.add( new WrappedNode( bundle, n ) );
        return result;
	}
	
	public List<WrappedNode> getValues( WrappedNode p ) {	
		List<WrappedNode> result = new ArrayList<WrappedNode>();
	//
		for (Statement s: r.listProperties( p.r.as(Property.class) ).toList() ) {	
			result.add( new WrappedNode( bundle, s.getObject() ) );
		}
	//
		return result;
	}
	
	public List<WrappedNode> getInverseValues( WrappedNode p ) {	
		List<WrappedNode> result = new ArrayList<WrappedNode>();
	//
		for (Statement s: r.getModel().listStatements(null, p.r.as(Property.class), basis).toList()) {	
			result.add( new WrappedNode( bundle, s.getSubject() ) );
		}
	//
		return result;
	}
	
	/**
	    Return a list of WrappedNodes corresponding to the distinct
	    predicates of properties of this WrappedNode. The order is
	    not [yet] specified. The property list is computed on first
	    request.
	*/
	public List<WrappedNode> getProperties() {
		if (properties == null) properties = coreGetProperties();
		return properties;
	}
	
	public List<WrappedNode> getInverseProperties() {
		if (inverses == null) inverses = coreGetInverseProperties();
		return inverses;
	}
	
	/**
	    Return a list of WrappedNodes corresponding to the distinct
	    predicates of properties of this WrappedNode.
	    
	*/
	private List<WrappedNode> coreGetProperties() {
		Set<WrappedNode> properties = new HashSet<WrappedNode>();
		ArrayList<WrappedNode> result = new ArrayList<WrappedNode>( properties );
		Set<Resource> seen = new HashSet<Resource>();
	//
		for (Statement s: r.listProperties().toList()) {
			Property p = s.getPredicate();
			// Brutal ad-hoc suppression of a known item endpoint loop.
			if (!p.equals(FOAF.isPrimaryTopicOf))
				if (seen.add(p)) result.add( new WrappedNode( bundle, p ) );
		}
	//
		return result;
	}
	
	private List<WrappedNode> coreGetInverseProperties() {
		Set<WrappedNode> properties = new HashSet<WrappedNode>();
		ArrayList<WrappedNode> result = new ArrayList<WrappedNode>( properties );
		Set<Resource> seen = new HashSet<Resource>();
	//
		for (Statement s: r.getModel().listStatements(null, null, basis).toList()) {
			Property p = s.getPredicate();
			if (seen.add(p)) result.add( new WrappedNode( bundle, p ) );
		}
	//
		return result;
	}

	public Resource asResource() {
		return r;
	}
	
	public void addPropertyValue( WrappedNode wp, WrappedNode o) {
//		List<WrappedNode> values = propertyValues.get( wp );
//		if (values == null)
//			propertyValues.put( wp, values = new ArrayList<WrappedNode>() );
//		values.add( o );
	}

	public void debugShow(PrintStream ps) {
//		if (isLiteral()) {
//			ps.print( " '" + basis.asLiteral().getLexicalForm() + "'" );
//		} else {
//			ps.print( "<<" + getLabel() + ">>" );
//			ps.print( " (" );
//			String and = "";
//			for (WrappedNode wp: propertyValues.keySet()) {
//				for (WrappedNode w: propertyValues.get( wp )) {					
//					ps.print( and ); and = "; ";
//					ps.print( wp.r.getLocalName() );
//					ps.print( " " );
//					w.debugShow( ps );
//				}
//			}
//			ps.print( ")" );
//		}
	}		
}