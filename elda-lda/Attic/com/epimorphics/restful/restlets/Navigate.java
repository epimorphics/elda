/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.restful.restlets;

import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.epimorphics.html.util.MatrixUtils;
import com.epimorphics.restful.support.CatalogueInJSON;
import com.epimorphics.restful.support.SharedConfig;
import com.epimorphics.sdx.system_state.ModelState;
import com.epimorphics.sdx.vocabulary.DSV;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

@Path("{a : anchor}") public class Navigate
    {
    final SharedConfig config;

    public Navigate( @Context UriInfo u ) 
        { config = SharedConfig.create( u ); }
    
    private static final Comparator<? super RDFNode> compareNodes = new Comparator<RDFNode>()
        {
        @Override public int compare( RDFNode a, RDFNode b )
            {
            return a.toString().compareToIgnoreCase( b.toString() );
            }
        };
    @GET @Path("{p : property}") @Produces("application/json") public String navigateJSON
        ( @PathParam("a") PathSegment a, @PathParam("p") PathSegment p )
        {
        CatalogueInJSON cj = new CatalogueInJSON( config );
        Model model = modelForNavigation( a, p );
        return cj.modelToJSONString( model );
        }

    private Model modelForNavigation( PathSegment a, PathSegment p )
        {
        Model result = ModelFactory.createDefaultModel();
        Model m = ModelState.getModel();
        MultivaluedMap<String, String> property = p.getMatrixParameters();
        Property px = m.createProperty( expandPrefix( m, property ) );
        Set<Resource> candidates = m.listSubjectsWithProperty( RDF.type, DSV.DataSet ).toSet();
        MatrixUtils.pruneCandidatesByProperties( m.getGraph().getPrefixMapping(), a, candidates );
        String filter = MatrixUtils.matrixFilter( a );
        List<RDFNode> values = new ArrayList<RDFNode>( getValues( px, candidates ) );
        Collections.sort( values, compareNodes );
        loadValues( result, filter, px, values );
        return result;
        }

    private String expandPrefix( Model m, MultivaluedMap<String, String> property )
        {
        String prefixed = property.get("p").get(0);
        try { return m.expandPrefix( prefixed ); }
        catch (NullPointerException e) { return prefixed; } // TODO fixme
        }

    private void loadValues( Model result, String filter, Property px, List<RDFNode> values )
        {
        String p = Util.urlEncode( Util.shortForm( px ) );
        for (RDFNode v: values)
            {
            String link = config.pathFor( "catalogue" + filter + ";" + p + "=" + Util.urlEncodeNode( v ) );
            Resource x = result.createResource();
            result.add( px, DSV.restrictionWith, x );
            result.add( x, DSV.hasValue, v );
            result.add( x, DSV.narrowerCatalogue, result.createResource( link ) );
            }
        }

    @GET @Path("{p : property}") @Produces("text/html") public String displayNavigate
        ( @PathParam("a") PathSegment a, @PathParam("p") PathSegment p )
        {
        Model m = ModelState.getModel();
        MultivaluedMap<String, String> property = p.getMatrixParameters();
        Property px = m.createProperty( expandPrefix( m, property ) );
        Set<Resource> candidates = m.listSubjectsWithProperty( RDF.type, DSV.DataSet ).toSet();
        MatrixUtils.pruneCandidatesByProperties( m.getGraph().getPrefixMapping(), a, candidates );
        String filter = MatrixUtils.matrixFilter( a );
        List<RDFNode> values = new ArrayList<RDFNode>( getValues( px, candidates ) );
        Collections.sort( values, compareNodes );
        return Util.withBody( "<h1>links for " + px + "</h1>\n" + linksFor( filter, px, values ) );
        }

    private Set<RDFNode> getValues( Property px, Set<Resource> candidates )
        {
        Set<RDFNode> result = new HashSet<RDFNode>();
        for (Resource r: candidates) 
            result.addAll( r.listProperties( px ).mapWith( Statement.Util.getObject ).toSet() );
        return result;
        }

    private String linksFor( String filter, Property px, List<RDFNode> values )
        {
        String p = Util.urlEncode( Util.shortForm( px ) );
        StringBuilder b = new StringBuilder();
        for (RDFNode v: values) 
            {
            String link = config.pathFor( "catalogue" + filter + ";" + p + "=" + Util.urlEncodeNode( v ) );
            b
                .append( "<div>" )
                .append( "<a href='" ).append( link ).append( "'>" ).append( v.toString() ).append( "</a>" )
                .append( "</div>\n" )
                ;
            }
        return b.toString();
        }
    }
