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
	
	protected RendererFactory theDefault; 
	
	public Factories() {
	}

	public Factories copy() {
		Factories result = new Factories();
		result.table.putAll( table );
		result.theDefault = theDefault;
		return result;
	}

	public void putFactory( String name, Resource uri, String mimeType, RendererFactory factory ) {
		putFactory( name, uri, mimeType, factory, false );
	}

	public void putFactory( String name, Resource uri, String mediaType, RendererFactory factory, boolean isDefault ) {
		RendererFactory f = factory.withResource( uri ).withMediaType( mediaType );
		table.put( name, f );
		if (isDefault) theDefault = f;
	}

	public RendererFactory getFactoryByName( String name ) {
		return table.get( name );
	}
	
	public RendererFactory getDefaultFactory() {
		return theDefault;
	}

//	public void debugPrint( Object x ) {
//		System.err.println();
//		System.err.println( ">> debug print table for " + x );
//		for (Map.Entry<String, RendererFactory> e: table.entrySet()) {
//			System.err.println( ">>  " + e.getKey() + " => " + e.getValue() );
//		}
//	}
}
