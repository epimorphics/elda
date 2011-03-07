package com.epimorphics.lda.renderers;

import java.util.HashMap;
import java.util.Map;

/**
    Named renderer factories. A renderer factory is just a
    way to make a renderer.
*/
public class Factories {
	
	protected final Map<String, RendererFactory> table = new HashMap<String, RendererFactory>();
	
	public Factories() {
	}

	public Factories copy() {
		Factories result = new Factories();
		result.table.putAll( table );
		return result;
	}

	public void putFactory( String name, RendererFactory factory ) {
		table.put( name, factory );
	}

	public RendererFactory getFactory( String name ) {
		return table.get( name );
	}
}
