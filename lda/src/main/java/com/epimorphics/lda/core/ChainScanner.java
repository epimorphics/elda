/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.lda.core;

import static com.epimorphics.util.CollectionUtils.a;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.ResourceUtils;
import com.epimorphics.lda.support.PropertyChain;

public class ChainScanner
	{
	private final List<Property> properties = new ArrayList<Property>();
	private final Set<Resource> roots = new HashSet<Resource>();
	private final Set<Statement> statements = new HashSet<Statement>();

	public static Model onlyMatchingChains(Model source, List<Resource> roots, List<PropertyChain> list) 
		{
		Model result = ModelFactory.createDefaultModel();
	    for (Resource root: roots) 
	    	{
	    	for (PropertyChain p: list) 
	    		{
	    		Model mm = extract( root.inModel(source), p, true );
	    		result.add( mm );
	    		}
	    	}
		return result;
		}

	public static Model extract( Resource root, PropertyChain chain, boolean withClosure ) 
		{
		ChainScanner s = new ChainScanner( root, chain.getProperties() );
		while (s.hasNext()) s = s.next();
		return s.finish( withClosure );
		}
	
	public ChainScanner( Resource root, List<Property> properties )
		{ this( a(root), new HashSet<Statement>(), properties ); }
	
	private ChainScanner( Set<Resource> roots, Set<Statement> statements, List<Property> properties )
		{
		this.roots.addAll( roots );
		this.properties.addAll( properties );	
		this.statements.addAll( statements );
		}
	
	public boolean hasNext()
		{ return properties.size() > 0; }
	
	private ChainScanner next()
		{
		Property p = properties.get(0);
		Set<Resource> newRoots = new HashSet<Resource>();
		Set<Statement> newStatements = new HashSet<Statement>( statements );
		for (Resource root: roots)
			for (Statement s: root.listProperties( p ).toList())
				{
				newStatements.add( s );
				if (s.getObject().isResource()) newRoots.add( s.getResource() );
				}
		return new ChainScanner( newRoots, newStatements, properties.subList( 1, properties.size() ) );
		}
	
	private Model finish( boolean withClosure ) 
		{
		Model result = ModelFactory.createDefaultModel();
		result.add( new ArrayList<Statement>( statements ) );
		if (withClosure)
			for (Resource root: roots)
				result.add( ResourceUtils.reachableClosure( root ) );
		return result;
		}
	}