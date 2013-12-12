/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.renderers;

import java.util.*;

import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Named renderer factories. (A renderer factory is just a
    way to make a renderer.) A <code>Factories</code> object can
    retrieve renderers by their name or their media type. 
*/
public class Factories {
	
	public static class FormatNameAndType {
		public final String name;
		public final String mediaType;
		
		public FormatNameAndType( String name, String mediaType ) {
			this.name = name;
			this.mediaType = mediaType;
		}
	}
	
	protected final Map<String, RendererFactory> nameToFactory = new HashMap<String, RendererFactory>();
	
	protected final Map<MediaType, RendererFactory> typeToFactory = new HashMap<MediaType, RendererFactory>();
	
	protected final Map<String, MediaType> nameToType = new HashMap<String, MediaType>();
	
	protected RendererFactory theDefault; 
	
	public Factories() { 
		theDefault = null;
	}
	
	public Factories copy() {
		Factories result = new Factories();
		result.nameToFactory.putAll( nameToFactory );
		result.typeToFactory.putAll( typeToFactory );
		result.nameToType.putAll( nameToType );
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
		nameToType.put( name, mt );
		if (isDefault) theDefault = f;
	}
	
	public Set<String> formatNames() {
		return nameToFactory.keySet();
	}
	
	public Set<FormatNameAndType> getFormatNamesAndTypes() {
		Set<FormatNameAndType> result = new HashSet<Factories.FormatNameAndType>();
		for (String name: formatNames()) 
			if (name.charAt(0) != '_') {
				String mediaType = getTypeForName( name ).toString();
				result.add( new FormatNameAndType(name, mediaType));
			}
		return result;
	}
	
	public MediaType getTypeForName( String name ) {
		return nameToType.get( name );
	}
	
	public RendererFactory getFactoryByType( MediaType mt ) {
		RendererFactory result = typeToFactory.get( mt );
		return result;
	}
	
	public RendererFactory getFactoryByName( String name ) {
		return nameToFactory.get( name );
	}
	
	public RendererFactory getDefaultFactory() {
		return theDefault;
	}
}
