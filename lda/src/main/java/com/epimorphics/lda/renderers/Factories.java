package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    Named renderer factories. A renderer factory is just a
    way to make a renderer.
*/
public class Factories {
	
	protected final Map<String, RendererFactory> table = new HashMap<String, RendererFactory>();
	protected final Map<Resource, RendererFactory> other = new HashMap<Resource, RendererFactory>();
	
	protected RendererFactory theDefault; 
	
	public Factories() {
	}

	public Factories copy() {
		Factories result = new Factories();
		result.table.putAll( table );
		result.other.putAll( other );
		result.theDefault = theDefault;
		return result;
	}

	public void putFactory( String name, Resource uri, String mimeType, RendererFactory factory ) {
		putFactory( name, uri, mimeType, factory, false );
	}

	public void putFactory( String name, Resource uri, String mimeType, RendererFactory factory, boolean isDefault ) {
		table.put( name, factory );
		other.put( uri, factory );
		if (isDefault) theDefault = factory;
	}

	public RendererFactory getFactoryByName( String name ) {
		return table.get( name );
	}
	
	public RendererFactory getDefaultFactory() {
		return theDefault;
	}

	public RendererFactory getFactoryByURI( Resource r ) {
		return other.get( r );
	}
}
