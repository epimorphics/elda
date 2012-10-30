package com.epimorphics.lda.renderers.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.support.PropertyChain;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

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
	
	/**
	    Initialise this ExtractByView with the view that defines what
	    it will extract.
	*/
	public ExtractByView( View view ) {
		this.v = view;
	}
	
	/**
	     Answer a list of the top-level tree copies of the selected items.
	*/
	public List<WrappedNode> itemise( List<Resource> items ) {
		List<WrappedNode> result = new ArrayList<WrappedNode>( items.size() );
		for (Resource i: items) result.add( copy( i, v.chains() ) );
		return result;
	}

	/**
	    Answer a new WrappedNode wrapping <code>r</code> and with the properties
	    available to it from the supplied property chains.
	*/
	private WrappedNode copy(Resource r, Set<PropertyChain> chains) {
		WrappedNode result = new WrappedNode( r );
		for (PropertyChain chain: chains) copy( result, chain.getProperties() );
		return result;
	}

	/**
	 	Add to <code>w</code> all the values of the first property in the
	 	list, if it is non-empty. Any values that are resources are wrapped
	 	and have their properties copied in turn. 
	*/
	private void copy(WrappedNode w, List<Property> properties) {
		if (properties.size() > 0) {
			Property p = properties.get(0);
			List<Property> rest = properties.subList( 1, properties.size() );
			if (w.isResource()) {
				for (Statement s: w.asResource().listProperties(p).toList()) {
					WrappedNode o = new WrappedNode( s.getObject() );
					copy( o, rest );
					w.addPropertyValue( new WrappedNode(s.getPredicate()), o );
				}
			}			
		}
	}

}
