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
import com.epimorphics.html.util.SDX_Utils;
import com.epimorphics.restful.support.CatalogueInJSON;
import com.epimorphics.restful.support.SharedConfig;
import com.epimorphics.sdx.system_state.ModelState;
import com.epimorphics.sdx.vocabulary.DSV;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

@Path("{c : catalogue}") public class Catalogue
    {
    final SharedConfig config;

    public Catalogue( @Context UriInfo u ) 
        { config = SharedConfig.create( u ); }
    
    @GET @Produces("text/html") public String displayCatalogue
        ( @PathParam("c") PathSegment c ) 
        { 
        Model m = ModelState.getModel();
        Set<Resource> candidates = m.listSubjectsWithProperty( RDF.type, DSV.DataSet ).toSet();
        MatrixUtils.pruneCandidatesByProperties( m.getGraph().getPrefixMapping(), c, candidates );
        return Util.withBody
            ( "<h1>catalogue</h1>\n" 
            + filterDisplay( c ) 
            + displayForCandidates( candidates ) 
            + displayForNavigation( c, candidates )
            ); 
        }
    
    @GET @Produces("application/json") public String displayCatalogueInJSON
        ( @PathParam("c") PathSegment c ) 
        { 
        CatalogueInJSON cj = new CatalogueInJSON( config );
        Model m = ModelState.getModel();
        Set<Resource> candidates = m.listSubjectsWithProperty( RDF.type, DSV.DataSet ).toSet();
        MatrixUtils.pruneCandidatesByProperties( m.getGraph().getPrefixMapping(), c, candidates );
        String catalogueURL = config.pathFor( "catalogue", MatrixUtils.matrixFilter( c ) );
        Resource catalogue = m.createResource( catalogueURL );
        Model output = cj.catalogueModelForCandidates( catalogue, c, candidates );
        return cj.modelToJSONString( output );
        }
    
    private String displayForNavigation( PathSegment c, Set<Resource> candidates )
        {
        String matrix = MatrixUtils.matrixFilter( c );
        Set<Property> properties = getAllProperties( candidates );
        StringBuilder b = new StringBuilder();
        b.append( "<div id='navigation'>\n" );
        b.append( "<h1>property navigation</h1>" );
        for (Property p: properties) b.append( displayForNavigation( matrix, p ) );
        b.append( "</div>" );
        return b.toString();
        }

    private String displayForNavigation( String matrix, Property p )
        {   
        String label = Util.niceName( p );
        String link = config.pathFor( "anchor" + matrix,  "property;p=" + Util.urlEncode( Util.shortForm( p ) ) );
        return
            "<div>"
            + "<a href='" + link + "'>" + label + "</a>"
            + "</div>\n"
            ;
        }

    public static Set<Property> getAllProperties( Set<Resource> candidates )
        {
        Set<Property> result = new HashSet<Property>();
        for (Resource r: candidates) result.addAll( r.listProperties().mapWith( Statement.Util.getPredicate ).toSet() );
        return result;
        }

    private String filterDisplay( PathSegment c )
        {
        StringBuilder b = new StringBuilder();
        MultivaluedMap<String, String> map = c.getMatrixParameters();
        boolean even = false;
        if (map.size() > 0) 
            {
            String gap = "";
            b.append( "<div style='margin-bottom: 1em'>" );
            b.append( "<span style='font-weight: bold'>filters:</span> " );
            for (Map.Entry<String, List<String>> entry : map.entrySet())
                {
                b
                    .append( gap )
                    .append( "<span>" )
                    .append( entry.getKey() )
                    .append( " " )
                    .append( showValues( c, entry.getKey(), entry.getValue() ) )
                    .append( "</span>" )
                    ;
                gap = "; ";
                even = !even;
                }
            b.append( "</div>" );
            }
        return b.toString();
        }

    private String showValues( PathSegment c, String key, List<String> values )
        {
        StringBuilder b = new StringBuilder();
        String gap = "";
        for (String s: values)
            {
            b.append( gap ).append( "<i>" ).append( Util.urlDecode( s ) ).append( "</i>" );
            b.append( removeLink( c, key, s ) );
            gap = ", ";
            }
        return b.toString();
        }

    private String removeLink( PathSegment c, String key, String value )
        {
        String link = config.pathFor( "catalogue" + MatrixUtils.matrixFilterWithout( c, key, value ) );
        return " <a href='" + link + "'>REMOVE</a>";
        }

    private String displayForCandidates( Set<Resource> candidates )
        {
        StringBuilder b = new StringBuilder();
        b.append( "<div id='candidates'>\n" );
        for (Resource r: candidates)
            {
            String label = SDX_Utils.labelOf( r ), id = SDX_Utils.idOf( r );
            String link = config.pathFor( "datasets", id );
            b
                .append( "<div>" )
                    .append( "<a href=" ).append( link ).append( ">" )
                    .append( label )
                    .append( "</a>" )
                .append( "</div>" )
                .append( "\n" )
                ;
            }
        b.append( "</div>\n" );
        return b.toString();
        }
    }
