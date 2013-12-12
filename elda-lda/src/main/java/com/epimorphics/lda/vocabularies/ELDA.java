/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.vocabularies;

import com.epimorphics.lda.Version;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.DOAP;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Elda-specific vocabulary and metadata.

    @author chris
*/
public class ELDA 
	{
	static final String version = Version.string;
	
	public static final String tag = "";
	
	static final private Model m = ModelFactory.createDefaultModel();
	
	static private Resource resource( String NS, String local ) 
		{ return m.createResource( NS + local ); }
	
	static private Property property( String NS, String local ) 
		{ return m.createProperty( NS + local ); }
	
	public static final Resource Elda = m.createResource();
	
	public static final Resource ThisElda = resource( EXTRAS.NS, "Elda_" + version );
	
	public static final Resource EldaRepository = resource( "https://elda.googlecode.com/hg/", "" );

	public static class DOAP_EXTRAS 
		{
		static final String NS = DOAP.NS;
		
	    public static final Property releaseOf = property( DOAP_EXTRAS.NS, "releaseOf" );
	    public static final Property _implements = property( DOAP_EXTRAS.NS, "implements" );
		}

	public static class COMMON 
		{
		public static final String NS = "http://purl.org/net/opmv/types/common#";
		
		public static final Property software = property( NS, "software" );
		}
    

	/**
	    Add the Elda processor metadata to the given resource <code>P</code>.
	*/
	public static void addEldaMetadata( Resource P ) 
		{
		Model m = P.getModel();
		P
			.addProperty( RDF.type, API.Service )
			.addProperty( COMMON.software, ThisElda )
			;
		ThisElda.inModel(m)
			.addProperty( RDFS.label, "Elda " + version + tag )
			.addProperty( RDF.type, DOAP.Version )
			.addProperty( DOAP.revision, version )
			.addProperty( ELDA.DOAP_EXTRAS.releaseOf, Elda );
		Elda.inModel(m)
			.addProperty( RDFS.label, "Elda" )
			.addProperty( DOAP.homepage, m.createResource( "http://elda.googlecode.com" ) )
			.addProperty( DOAP.wiki, m.createResource( "http://code.google.com/p/elda/w/list" ) )
			.addProperty( DOAP.bug_database, m.createResource( "http://code.google.com/p/elda/issues/list" ) )
			.addProperty( DOAP.programming_language, "Java" )
			.addProperty( DOAP.repository, EldaRepository )
			.addProperty( ELDA.DOAP_EXTRAS._implements, "http://code.google.com/p/linked-data-api/wiki/Specification" )
			;
		EldaRepository.inModel(m)
			.addProperty( RDF.type, DOAP.Repository )
			.addProperty( DOAP.location, m.createResource( "https://elda.googlecode.com" ) )
			.addProperty( DOAP.browse, m.createResource( "http://code.google.com/p/elda/source/browse/" ) )
			;
		}

	}
