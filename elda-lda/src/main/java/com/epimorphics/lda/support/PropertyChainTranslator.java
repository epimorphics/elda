/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.support;

import java.util.*;

import com.epimorphics.lda.core.VarSupply;
import com.hp.hpl.jena.rdf.model.Property;

/**
    A PropertyChainTranslator is initialised with a bunch of property chains
    and can then be used to create components of a SPARQL query.
    
    <p>Each property chain X = P.Q ... generates a SPARQL clause
    OPTIONAL {?item P ?v1. OPTIONAL {?v1 Q ?v2}} etc. The variables 
    are allocated by a VarSupply supplied as a parameter.
    
 	@author chris
*/
public class PropertyChainTranslator
	{
	private final PropertyChain [] chains;
	
	/**
	    Initialise this translator with the given list of property chains.
	*/
	public PropertyChainTranslator( List<PropertyChain> chains )
		{ this( chains.toArray( new PropertyChain[chains.size()] ) ); 
		// System.err.println( ">> PCT: chains = " + Arrays.asList( chains ) );
		}

	/**
	    Initialise this translator with the given array of property chains.
	*/
	public PropertyChainTranslator( PropertyChain...chains )
		{ this.chains = chains; }
	
	/**
	    Translate this collection of property chains to the corresponding
	    SPARQL clauses. The initial subject variable name is "item".
	    Allocate new variables from the VarSupply.
	*/
	public String translate( VarSupply vs, boolean dropLast ) 
		{ return translate( vs, "item", dropLast ); }

	/**
	    Translate this collection of property chains to the corresponding
	    SPARQL optional clauses. The initial subject variable name is given
	    by subjectVar. Allocate new variables from the VarSupply.
	*/
	public String translate( VarSupply vs, String subjectVar, boolean dropLast ) 
		{ return translate( new Vars(vs), subjectVar, dropLast );	}

	public String translate( Vars vars, String subjectVar, boolean dropLast ) {
		StringBuilder result = new StringBuilder();
		Set<List<Property>> choppedChains = getChoppedChains( dropLast );
		if (choppedChains.size() > 0)
			{
			result.append( "{ {}\n" );
			for (List<Property> lp: choppedChains) translateAsUNION( result, vars, subjectVar, lp );
			result.append( "}" );			
			}
		return result.toString();
	}

	private Set<List<Property>> getChoppedChains(boolean dropLast) 
		{
		Set<List<Property>> choppedChains = new HashSet<List<Property>>();
		for (PropertyChain pc: chains) 
			{
			List<Property> chain = mayTrim( pc.getProperties(), dropLast );
			if (chain.size() > 0) choppedChains.add( chain );
			}
		return choppedChains;
		}
	
	private void translateAsUNION(StringBuilder result, Vars vs, String S, List<Property> pc) 
		{
		if (pc.size() > 0)
			{
			Property p = pc.get(0);
			result.append( "UNION {" );
			String v = vs.var(pc.size());
			result.append( " " ).append( "?" ).append( S );
			result.append( " " ).append( "<" ).append( p.getURI() ).append( ">" );
			result.append( " " ).append( "?" ).append(v);
			translateAsOPTIONAL( result, vs, v, pc.subList( 1, pc.size() ) );
			result.append( "}" );
			}
		}

	private List<Property> mayTrim( List<Property> l, boolean dropLast ) 
		{ return dropLast ? l.subList( 0, l.size() - 1 ) : l; }

	/**
	    Translate one property chain to SPARQL optionals. Assumes that there 
	    are only named resources and variables to unwrap into the clause.
	    Uses recursion for the non-first elements of the list.
	*/
	private void translateAsOPTIONAL(StringBuilder result, Vars vs, String S, List<Property> pc) 
		{
		if (pc.size() > 0)
			{
			result.append( "\nOPTIONAL {" );
			Property p = pc.get(0);
			String v = vs.var(pc.size());
			result.append( " " ).append( "?" ).append( S );
			result.append( " " ).append( "<" ).append( p.getURI() ).append( ">" );
			result.append( " " ).append( "?" ).append(v);
			result.append( " ." );
			translateAsOPTIONAL( result, vs, v, pc.subList(1, pc.size() ) );
			result.append( " }" );
			}
		}
	
	public static class Vars
		{
		private final VarSupply vs;
		private final Map<Integer,String> map = new HashMap<Integer, String>();
		
		public Vars(VarSupply vs) 
			{ this.vs = vs; }

		public String var(int i) 
			{
			String v = map.get(i);
			if (v == null) map.put(i, v = stripAnyLeadingQMark(vs.newVar().name() ) ); 
			return v;
			}
		
		private String stripAnyLeadingQMark( String v ) 
			{ return v.startsWith("?") ? v.substring(1) : v; }
		}
	}