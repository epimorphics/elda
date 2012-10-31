package com.epimorphics.lda.renderers.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.View.Type;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    <p>
    	ExtractByView makes a nested structure of WrappedNodes which is
    	the view property chains overload on the result model.
    </p>
    
    <p>
    	Driving the rendering off the view chains has two useful related
    	effects: there is no need to check for circularity in the rendering,
    	since we forcibly generate a tree of WrappedNodes, and there is 
    	reduced danger of the metadata being pulled into the tree.
    </p>
*/
public class ExtractByView {
	
	final View v;
	final ShortNames sn;
	
	/**
	    Initialise this ExtractByView with the view that defines what
	    it will extract.
	*/
	public ExtractByView( ShortNames sn, View v ) {
		this.sn = sn;
		this.v = v;
	}
	
	/**
	     Answer a list of the top-level tree copies of the selected items.
	*/
	public List<WrappedNode> itemise( List<Resource> items ) {
		List<WrappedNode> result = new ArrayList<WrappedNode>( items.size() );
		int describeState = createDescribeState( v.getType() );
		for (Resource i: items) result.add( copy( i, getChains(), describeState ) );
		return result;
	}

	static final Property fakeProperty = ResourceFactory.createProperty( "fake:/property" );
	
	static final PropertyChain fakeChain = makeFakeChain();
	
	private Set<PropertyChain> getChains() {
		Set<PropertyChain> result = v.chains();
		if (result.isEmpty()) result = CollectionUtils.set( fakeChain );
		return result;
	}

	private static PropertyChain makeFakeChain() {
		List<Property> properties = new ArrayList<Property>();
		properties.add( fakeProperty );
		properties.add( fakeProperty );
		properties.add( fakeProperty );
		return new PropertyChain( properties );
	}

	private int createDescribeState( Type type ) {
		switch (type) {
		case T_ALL: return 1;
		case T_DESCRIBE: return 0;
		default: return -1;
		}
	}

	/**
	    Answer a new WrappedNode wrapping <code>r</code> and with the properties
	    available to it from the supplied property chains.
	*/
	private WrappedNode copy(Resource r, Set<PropertyChain> chains, int describeState) {
		WrappedNode result = new WrappedNode( sn, r );
		for (PropertyChain chain: chains) copy( result, chain.getProperties(), describeState );
		return result;
	}

	/**
	 	Add to <code>w</code> all the values of the first property in the
	 	list, if it is non-empty. Any values that are resources are wrapped
	 	and have their properties copied in turn. 
	*/
	private void copy(WrappedNode w, List<Property> properties, int describeState) {
		if (properties.size() > 0) {
			Property p = properties.get(0);
			List<Property> rest = properties.subList( 1, properties.size() );
			if (w.isResource()) {
				int nextState = w.isAnon() ? describeState : describeState - 1;
				for (Statement s: statementsFor(w, p, describeState)) {
					WrappedNode o = new WrappedNode( sn, s.getObject() );
					copy( o, rest, nextState );
					w.addPropertyValue( new WrappedNode( sn, s.getPredicate()), o );
				}
			}			
		}
	}

	/**
	    <p>
	    Answer the statements with subject [the resource of] <code>w</code> 
	    and predicate <code>p</code>. If <code>p</code> is <code>propertySTAR</code>,
	    or these statements are for a DESCRIBE, allow any property. If we're doing the
	    trailing labels of a DESCRIBE ALL, allow any rdfs:labels as well as whatever
	    <code>p</code> allows.
	    </p>
	*/
	private List<Statement> statementsFor( WrappedNode w, Property p, int describeState ) {
		Resource r = w.asResource();
		if (describeState == 1 || p.equals( ShortnameService.Util.propertySTAR)) 
			return r.listProperties().toList();
		else {
			Set<Statement> result = r.listProperties(p).toSet();
			if (describeState == 0) result.addAll( r.listProperties( RDFS.label).toSet() );
			return new ArrayList<Statement>( result );
		}
	}

}
