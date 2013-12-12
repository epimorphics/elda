/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.restful.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.ws.rs.core.*;

import org.junit.Ignore;
import org.junit.Test;

import com.epimorphics.restful.support.*;
import com.epimorphics.sdx.vocabulary.DSV;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class TestCatalogueModelConstruction 
	{
	private final class MyUriInfo implements UriInfo
        {
        protected final String base;
        
        public MyUriInfo( String base )
            { this.base = base; }

        @Override public UriBuilder getRequestUriBuilder()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public URI getRequestUri()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public MultivaluedMap<String, String> getQueryParameters(
                boolean decode )
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public MultivaluedMap<String, String> getQueryParameters()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public List<PathSegment> getPathSegments( boolean decode )
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public List<PathSegment> getPathSegments()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public MultivaluedMap<String, String> getPathParameters(
                boolean decode )
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public MultivaluedMap<String, String> getPathParameters()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public String getPath( boolean decode )
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public String getPath()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public List<String> getMatchedURIs( boolean decode )
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public List<String> getMatchedURIs()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public List<Object> getMatchedResources()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public UriBuilder getBaseUriBuilder()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public URI getBaseUri()
            { try
                { return new URI( base ); }
            catch (URISyntaxException e)
                { throw new WrappedException( e ); }
            }

        @Override public UriBuilder getAbsolutePathBuilder()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public URI getAbsolutePath()
            {
            //  Auto-generated method stub
            return null;
            }
        }

    private final class TrivialPathSegment implements PathSegment
        {
        @Override public String getPath()
            {
            //  Auto-generated method stub
            return null;
            }

        @Override public MultivaluedMap<String, String> getMatrixParameters()
            { return new MultivaluedMapImpl(); }
        }

    @Test public void testEmptyCatalogue()
		{
		Model empty = ModelFactory.createDefaultModel();
		Resource root = empty.createResource( "eh:/x" );
		SharedConfig config = SharedConfig.create( null );
		CatalogueInJSON cj = new CatalogueInJSON( config );
		PathSegment c = new TrivialPathSegment();
		Model out = cj.catalogueModelForCandidates( root, c, new HashSet<Resource>() );
		Model expected = model( "eh:/x dsv:hasURL eh:/x; eh:/x rdf:type dsv:Catalogue" );
		ModelTestBase.assertIsoModels( expected, out );
		}
    
    @Test public void testSimpleCatalogue()
        {
        Model given = model( "R rdf:type dsv:DataSet; R dsv:hasID 'xxx'" );
        Resource root = given.createResource( "eh:/x" );
        UriInfo u = new MyUriInfo( "eh:/base#" );        
        CatalogueInJSON cj = new CatalogueInJSON( SharedConfig.create( u ) );
        PathSegment c = new TrivialPathSegment();
        HashSet<Resource> candidates = new HashSet<Resource>();
        candidates.add( given.createResource( "eh:/R" ) );
        Model out = cj.catalogueModelForCandidates( root, c, candidates );
        Model expected = xxx( given );
        ModelTestBase.assertIsoModels( expected, out );
        }

    private Model xxx( Model given )
        {
        Model expected = model
            ( "eh:/x dsv:hasURL eh:/x; eh:/x rdf:type dsv:Catalogue" 
            + "; eh:/x dsv:hasProperty _p1"
            );
        Resource root = expected.createResource( "eh:/x" );
        Resource rrr = expected.createResource( "eh:/base#datasets/xxx" );
        root.addProperty( DSV.hasEntry, rrr );
        rrr.addProperty( RDFS.label, "xxx" );
        rrr.addProperty( RDF.type, DSV.DataSet );
        addPropertyDescription( expected, RDF.type, "_p1", root );
        addPropertyDescription( expected, DSV.hasID, "_p2", root );
        return expected;
        }

    private void addPropertyDescription( Model expected, Property p, String lll, Resource root )
        {
        Resource _p1 = expected.createResource( new AnonId( lll ) );
        expected.add( root, DSV.hasProperty, _p1 );
        expected.add( _p1, DSV.propertyName, p );
        expected.add( _p1, DSV.valuesFor, expected.createResource( "eh:/base#anchor/property;p=" + encode( expected, p ) ) );
        }

    private String encode( PrefixMapping pm, Property p )
        {
        return pm.shortForm( p.getURI() ).replace( ":", "%3A" );
        }

    private Model model( String terms )
        {
        Model result = ModelFactory.createDefaultModel();
        result.setNsPrefixes( PrefixMapping.Extended );
        result.setNsPrefix( "dsv", DSV.getURI() );
        ModelTestBase.modelAdd( result, terms );
        return result;
        }
	}
