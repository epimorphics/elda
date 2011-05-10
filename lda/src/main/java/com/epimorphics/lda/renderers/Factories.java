package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Named renderer factories. A renderer factory is just a
    way to make a renderer.
*/
public class Factories {
	
	protected final Map<String, RendererFactory> table = new HashMap<String, RendererFactory>();
	
	protected RendererFactory theDefault; 
	
	public Factories() { 
		theDefault = null;
	}

	public Factories copy() {
		Factories result = new Factories();
		result.table.putAll( table );
		result.theDefault = theDefault;
		return result;
	}

	public void putFactory( String name, Resource uri, MediaType mt, RendererFactory factory ) {
		putFactory( name, uri, mt, factory, false );
	}

	public void putFactory( String name, Resource uri, MediaType mt, RendererFactory factory, boolean isDefault ) {
		RendererFactory f = factory.withRoot( uri ).withMediaType( mt );
		table.put( name, f );
		if (isDefault) theDefault = f;
	}

	public RendererFactory getFactoryByName( String name ) {
		return table.get( name );
	}
	
	public RendererFactory getDefaultFactory() {
		return theDefault;
	}
}
