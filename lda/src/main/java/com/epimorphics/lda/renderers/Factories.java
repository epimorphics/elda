package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Named renderer factories. (A renderer factory is just a
    way to make a renderer.) A <code>Factories</code> object can
    retrieve renderers by their name or their media type. 
*/
public class Factories {
	
	protected final Map<String, RendererFactory> nameToFactory = new HashMap<String, RendererFactory>();
	
	protected final Map<MediaType, RendererFactory> typeToFactory = new HashMap<MediaType, RendererFactory>();
	
	protected RendererFactory theDefault; 
	
	public Factories() { 
		theDefault = null;
	}
	
	public Factories copy() {
		Factories result = new Factories();
		result.nameToFactory.putAll( nameToFactory );
		result.typeToFactory.putAll( typeToFactory );
		result.theDefault = theDefault;
		return result;
	}

	public void putFactory( String name, Resource uri, MediaType mt, RendererFactory factory ) {
		putFactory( name, uri, mt, factory, false );
	}

	public void putFactory( String name, Resource uri, MediaType mt, RendererFactory factory, boolean isDefault ) {
		RendererFactory f = factory.withRoot( uri ).withMediaType( mt );
		nameToFactory.put( name, f );
		typeToFactory.put( mt, f );
		if (isDefault) theDefault = f;
	}

	public RendererFactory getFactoryByType( MediaType mt ) {
		return typeToFactory.get( mt );
	}
	
	public RendererFactory getFactoryByName( String name ) {
		return nameToFactory.get( name );
	}
	
	public RendererFactory getDefaultFactory() {
		return theDefault;
	}
}
