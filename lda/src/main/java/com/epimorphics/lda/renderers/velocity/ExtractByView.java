package com.epimorphics.lda.renderers.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.epimorphics.lda.core.View;
import com.epimorphics.lda.support.PropertyChain;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ExtractByView {
	
	final View v;
	
	public ExtractByView( View view ) {
		this.v = view;
	}
	
	public List<WrappedNode> itemise( List<Resource> items ) {
		List<WrappedNode> result = new ArrayList<WrappedNode>( items.size() );
		for (Resource i: items) result.add( copy( i, v.chains() ) );
		return result;
	}

	private WrappedNode copy(Resource r, Set<PropertyChain> chains) {
		WrappedNode result = new WrappedNode( r );
		for (PropertyChain chain: chains) copy( result, chain.getProperties() );
		return result;
	}

	private void copy(WrappedNode w, List<Property> properties) {
		if (properties.size() > 0) {
			Property p = properties.get(0);
			List<Property> rest = properties.subList( 1, properties.size() );
			if (w.isResource()) {
				for (Statement s: w.asResource().listProperties(p).toList()) {
					WrappedNode o = new WrappedNode( s.getObject() );
					copy( o, rest );
					w.addPropertyValue( s.getPredicate(), o );
				}
			}			
		}
	}

}
