package com.epimorphics.jsonrdf;

import java.util.Set;

import com.hp.hpl.jena.rdf.model.Property;

/**
    The operations of a Context [which implements this] which are needed
    to render JSON.
*/

public interface ReadContext {

	public ContextPropertyInfo findProperty(Property p);
	
	public boolean isSortProperties();
	
	public String getNameForURI(String uri);
	
	public String getURIfromName(String code);
	
	public String getBase();
	
	public String forceShorten(String uri);
	
	public Set<String> allNames();
	
	public ContextPropertyInfo getPropertyByName(String name);
}

