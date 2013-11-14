package com.epimorphics.lda.renderers.velocity;

import java.net.URI;
import java.util.*;

import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.Transcoding;
import com.epimorphics.util.URIUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    A WrappedNode is an RDF node wrapped in a shell of useful methods to be
    called from Velocity templates.
*/
public class WrappedNode implements Comparable<WrappedNode> {
	
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
	
	/**
		A WrappedNode is .equals to another object if
		that object is a WrappedNode and their underlying
		RDFNodes are equal.
	*/
	public boolean equals( Object other ) {
		return other instanceof WrappedNode && basis.equals( ((WrappedNode) other).basis );
	}
	
	public WrappedNode( ShortNames sn, IdMap ids, RDFNode r ) {
		this( new Bundle( sn, ids ), r );
	}
	
	public WrappedNode( Bundle b, RDFNode r ) {
		this.bundle = b;
		this.r = (r.isResource() ? r.asResource() : null);
		this.label = (this.r == null ? "NONE" : Help.labelFor( this.r ) );
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
	
	/**
	    Returns <code>r</code> wrapped using the same bundle
	    as this WrappedNode.
	*/
	public WrappedNode wrap( Resource r ) {
		return new WrappedNode( bundle, r );
	}
	
	public WrappedNode wrap( RDFNode n ) {
		return new WrappedNode( bundle, n );
	}
	
	/**
	    Returns <code>uri</code> wrapped as a Resource using 
	    the same bundle as this WrappedNode.
	*/
	public WrappedNode wrap( String uri ) {
		return new WrappedNode( bundle, r.getModel().createResource(uri) );
	}
	
	/**
		Return this wrapped resource's preferred label;
		the first literal of (a) this resources skos:prefLabel,
		(b) an unlanguaged rdfs:label, (c) a languaged
		rdfs:label, (d) the local name of this resource.
	*/
	public WrappedString getLabel() {
		return new WrappedString( label );
	}

	@Override public int compareTo( WrappedNode o ) {
		return toString().compareToIgnoreCase( o.toString() );
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
	
	/**
		If this node is a wrapped Resource, return the
		shortname associated with that Resource or its
		localname if it has no shortname. If this node
		is a wrapped Literal, return the lexical form of
		that literal.
	*/
	public WrappedString shortForm() {
		if (r == null) return shortLiteral();
		return shortURI();
	}
	
	private WrappedString shortURI() {
		String shortish = bundle.sn.getWithUpdate(r);
		return new WrappedString( tidyPrefix(shortish) );
	}

	private String tidyPrefix(String shortish) {
		int ub = shortish.indexOf('_');
		if (ub < 0) return shortish;
	//
		String pre = shortish.substring(0, ub);
		if (pre.equals("pre")) {
			int ub2 = shortish.indexOf('_', 4);
			String prefix = shortish.substring(ub + 1, ub2);
			return prefix + ": " + decode(shortish.substring(ub2 + 1));
		} else {
			return pre + ": " + shortish.substring(pre.length() + 1);
		}
	}

	private String decode(String s) {
		return Transcoding.decodeLightly(s);
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
		return change( prefix, p.shortForm().content, v.toString() );
	}
	
	public WrappedNode change( String prefix, String p, String v ) {
		
//		if (p.equals("_properties")) System.err.println( ">> changing " + this.getURI() + " )) " + p + " to " + v );
		URI ru = URIUtils.newURI( r.getURI() );
		String key = prefix + p;
		String value = v.toString();
		URI u = URIUtils.replaceQueryParam( ru, key, value );	
	//
		Resource changed = r.getModel().createResource( u.toString() );
		
//		if (p.equals("_properties")) System.err.println( ">>   ==> " + changed );
		
		return new WrappedNode( bundle, changed );
	}
	
	/**
	 	If this WrappedNode is a Resource, return its URI.
	*/
	public WrappedString getURI() {
		return new WrappedString( r.getURI() );
	}
	
	/**
	 	Return true iff this WrappedNode is a wrapped Literal.
	*/
	public boolean isLiteral() {
		return basis.isLiteral();
	}
	
	public boolean isSelfQuoting() {
		return basis.isLiteral() && isNumber(basis.asLiteral());
	}
	
	protected boolean isNumber(Literal l) {
		RDFDatatype dt = l.getDatatype();
		return dt == null ? false : isNumericType(dt.getURI());
	}
	
	// TODO probably want a set for these. And if we expose
	// it perhaps we don't need isSelfQuoting.
	private boolean isNumericType(String uri) {
		return 
			uri.equals(XSDDatatype.XSDbyte.getURI())
			|| uri.equals(XSDDatatype.XSDshort.getURI())
			|| uri.equals(XSDDatatype.XSDint.getURI())
			|| uri.equals(XSDDatatype.XSDinteger.getURI())
			|| uri.equals(XSDDatatype.XSDdecimal.getURI())
			|| uri.equals(XSDDatatype.XSDfloat.getURI())
			|| uri.equals(XSDDatatype.XSDdouble.getURI())
			|| uri.equals(XSDDatatype.XSDboolean.getURI())
			|| uri.equals(XSDDatatype.XSDdate.getURI())
			;
	}

	/**
	 	Return true iff this WrappedNode is a wrapped Resource
	 	(which might be a blank node).
	*/
	public boolean isResource() {
		return basis.isResource();
	}

	/**
	 	Return true iff this WrappedNode is a wrapped blank node.
	*/
	public boolean isAnon() {
		return basis.isAnon();
	}
	
	/**
		If this node is a wrapped Literal, return its language
		if any, otherwise return the empty string.
	*/
	public String getLanguage() {
		return basis.asLiteral().getLanguage();
	}
	
	/**
	 	Return the short form of the URI representing the type
		of this wrapped literal node.
	*/
	public WrappedString getLiteralType() {
		String t = basis.asLiteral().getDatatypeURI();
		if (t == null) return new WrappedString( "" );
		return new WrappedString( bundle.sn.getWithUpdate( t ) );
	}
	
	/**
	    Return the value of the wrapped literal
	*/
	public Object getLiteralValue() {
		return basis.asLiteral().getValue();
	}
	
	/**
	    Return true iff this WrappedNode wraps a Resource representing
		an RDF list.
	*/
	public boolean isList() {
		return basis.isAnon() && basis.asResource().canAs( RDFList.class );
	}
	
	/**
	    Return a Java list of WrappedNodes wrapping the elements of
		the RDF list represented by this WrappedNode.
	*/
	public List<WrappedNode> asList() {
        List<RDFNode> rawlist = basis.as( RDFList.class ).asJavaList();
        List<WrappedNode> result = new ArrayList<WrappedNode>( rawlist.size() );
        for (RDFNode n : rawlist) result.add( new WrappedNode( bundle, n ) );
        return result;
	}
	
	/**
	    Return a Java list of WrappedNodes which are the
		objects of all statements for which this WrappedNode
		is the subject and the argument <code>property</code>
		is the predicate. (This argument will typically be
		an element from the <code>getProperties</code> list.)
	*/
	public List<WrappedNode> getValues( WrappedNode p ) {	
		List<WrappedNode> result = new ArrayList<WrappedNode>();
	//
		if (basis.isResource())
			for (Statement s: r.listProperties( p.r.as(Property.class) ).toList() ) {	
			result.add( new WrappedNode( bundle, s.getObject() ) );
			}
	//
		return result;
	}
	
	public WrappedNode valueOf(String qName) {
		Model m = r.getModel();

		// HACK
		if (r.getModel().getNsPrefixMap().isEmpty())
			APIResultSet.setUsedPrefixes(m, PrefixMapping.Standard);		
		
		Property p = m.createProperty( m.expandPrefix(qName) );
		Statement s = r.getProperty(p);
		
		// System.err.println( ">> " + qName + " ==> " + (s == null ? "NONE" : s.getObject() ) );
		
		return s == null ? null : wrap(s.getObject());
	}
	
	/**
		Return a Java list of WrappedNodes which are the
		subjects of all statements for which this WrappedNode
		is the object and the argument <code>property</code>
		is the predicate. (This argument will typically be
		an element from the <code>getInverseProperties</code> list.)
	*/
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
	
	/**
	    Return a Java list of WrappedNodes which are
		the wrapped form of predicates P where there is some
		subject S such that (S, P, this wrapped node).
	*/
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
		return sort( result );
	}

	/**
	    Return a list of WrappedNodes corresponding to the distinct
	    predicates of inverse properties of this WrappedNode.
	    
	*/
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
		return sort( result );
	}
	
	/**
	    A sortable item wrapping a property node. The label is to toString()
	    of the property, computed once. <code>hasLiterals</code> is precomputed
	    and is true if any of the values of this property with respect to
	    the given subject node S are literals.	    
	*/
	static class Sortable implements Comparable<Sortable> {
		final WrappedNode property;
		final String label;
		final boolean hasLiterals;
		
		Sortable(WrappedNode S, WrappedNode property) {
			this.property = property;
			this.label = property.toString();
			this.hasLiterals = hasLiterals(S, property);
		}

		@Override public int compareTo(Sortable other) {
			if (hasLiterals == other.hasLiterals)
				return label.compareToIgnoreCase(other.label);
			return hasLiterals ? -1 : +1;
		}
		
		private static boolean hasLiterals(WrappedNode S, WrappedNode property) {
			StmtIterator s = S.r.listProperties(property.r.as(Property.class));
			while (s.hasNext())	if (s.next().getObject().isLiteral()) return true;
			return false;
		}
	}
	
	/**
	    Sort <code>nodes</code> as properties with respect to this resource
	    node. Properties which have literal values sort before those that
	    have resource values. Otherwise they sort according to their toString().
	    [This will probably change, since numbers don't sort property on
	    lexical forms.]
	*/
	private List<WrappedNode> sort(ArrayList<WrappedNode> nodes) {
		List<Sortable> toSort = new ArrayList<Sortable>(nodes.size() );
		for (WrappedNode n: nodes) toSort.add(new Sortable( this, n ) );		
		Collections.sort( toSort );
		List<WrappedNode> result = new ArrayList<WrappedNode>( nodes.size() );
		for (Sortable s: toSort) result.add( s.property );
		return result;
	}

	public Resource asResource() {
		return r;
	}	
}