/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.epimorphics.lda.core.property.ViewProperty;
import com.epimorphics.util.CollectionUtils;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
    A PropertyChain is a sequence of properties, to be used to trawl
    down a model.
    
 	@author chris
*/
public class PropertyChain {
	
	protected final List<ViewProperty> chain;
	
	public PropertyChain(List<ViewProperty> chain) {
		this.chain = chain;
	}
	
	@Override public boolean equals( Object other ) {
		return other instanceof PropertyChain 
			&& chain.equals( ((PropertyChain) other).chain );
	}
	
	@Override public int hashCode() {
		return chain.hashCode();
	}
	
	public PropertyChain( ViewProperty p ) {
		this( CollectionUtils.list(p) );
	}
	
	@Override public String toString() {
		return chain.toString();
	}

	public PropertyChain tail() {
		return new PropertyChain(chain.subList(1, chain.size()));
	}

	public List<ViewProperty> getProperties() {
		return new ArrayList<>(chain);
	}
}