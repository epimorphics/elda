/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.restful.restlets;

import java.util.List;
import java.util.Set;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import com.epimorphics.html.util.SDX_Utils;
import com.epimorphics.restful.support.CatalogueInJSON;
import com.epimorphics.restful.support.SharedConfig;
import com.epimorphics.sdx.system_state.ModelState;
import com.epimorphics.sdx.vocabulary.DSV;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.ResourceUtils;

@Path("/datasets/") public class DataSets
    {
    final SharedConfig config;
    
    public DataSets( @Context UriInfo u ) 
        { config = SharedConfig.create( u ); }
    
    @GET @Produces("application/json") public String renderAllDatasetsAsJSON() 
        { 
        Model m = ModelState.getModel();
        Model result = ModelFactory.createDefaultModel();       
        String catalogueURL = config.pathFor( "catalogue" );
        Resource root = result.createResource( catalogueURL );
        CatalogueInJSON cj = new CatalogueInJSON( config );
        for (Resource entry: m.listSubjectsWithProperty( DSV.hasID, (RDFNode) null ).toList()) 
        	{
        	String id = SDX_Utils.idOf( entry ), label = SDX_Utils.labelOf( entry );
        	result.add( root, DSV.hasEntry, entry );
        	result.add( entry, DSV.hasID, id );
        	result.add( entry, RDFS.label, label );
        	};
        return cj.modelToJSONString( result );
        }
    
    @GET @Produces("text/html") public String displayAllDatasets() 
        { 
        Model m = ModelState.getModel();
        String result = htmlForDatasets( m );
        return Util.withBody( "<h1>datasets</h1>\n" + result ); 
        }
    
    @GET @Path("/{id}") @Produces("text/html") public String displayDataset( @PathParam("id") String id )
        {
        Model m = ModelState.getModel();
        Resource r = m.listSubjectsWithProperty( DSV.hasID, id ).next();
        StringBuilder b = new StringBuilder();
        b.append( "<h1>" ).append( SDX_Utils.labelOf( r ) ).append( "</h1>" );
        displayResourceProperties( b, r );
        return Util.withBody( b.toString() );
        }
    
    @GET @Path("/{id}") @Produces("application/json") public String datasetAsJSON( @PathParam("id") String id )
        {
        Model m = ModelState.getModel();
        CatalogueInJSON cj = new CatalogueInJSON( config );
        Resource r = m.listSubjectsWithProperty( DSV.hasID, id ).next();
        return cj.modelToJSONString( ResourceUtils.reachableClosure( r ) );
        }

    private void displayResourceProperties( StringBuilder b, Resource r )
        {
        Set<Property> properties = r.listProperties().mapWith( Statement.Util.getPredicate ).toSet();
        for (Property prop: properties)
            {
            String gap = "";
            b.append( "<div class='property'>" ).append( Util.shortForm( prop ) ).append( "</div>\n" );
            b.append( "<div class='values'>" );
            for (Statement s: r.listProperties( prop ).toList())
                {
                String o = s.getObject().toString();
                b.append( gap ).append( "<span class='value'>" ).append( o ).append( "</span>" );
                gap = ", ";
                }
            b.append( "</div>" );
            }
        }
    
    @POST @Produces("text/html") public String sneakyInstall()
        {
        Model tdbm = ModelState.getModel();
        Model m = EldaFileManager.get().loadModel( "/tmp/install.ttl" );
        tdbm.add( m ).setNsPrefixes( m );
        Resource type = m.createResource( m.expandPrefix( "ckan-data:Package" ) );
        for (Resource r: m.listSubjectsWithProperty( RDF.type, type ).toList())
            {
            String id = ModelState.createNewId( tdbm );
            Resource r2 = r.inModel( tdbm );
            RDFNode title = getLabelFor( r, id );
            r2
                .addProperty( RDF.type, DSV.DataSet )
                .addProperty( DSV.hasID, id )
                .addProperty( RDFS.label, title )
                ;
            }
        TDB.sync( tdbm );
        return Util.withBody( "DONE" );
        }
    
    private RDFNode getLabelFor( Resource r, String id )
        {
        List<RDFNode> labels = r.listProperties( DC.title ).mapWith( Statement.Util.getObject ).toList();
        return labels.isEmpty() ? r.getModel().createLiteral( id ) : labels.get( 0 );
        }

    private String htmlForDatasets( Model m )
        {
        StringBuilder result = new StringBuilder();
        ResIterator st = m.listSubjectsWithProperty( RDF.type, DSV.DataSet );
        while (st.hasNext())
            {
            Resource ds = st.nextResource();
            result.append( linkFor( ds ) ).append( "\n" );
            }
        return result.toString();
        }

    private String linkFor( Resource ds )
        {
        String label = SDX_Utils.labelOf( ds );
        String link = config.pathFor( "datasets", SDX_Utils.idOf( ds ) );
        return "<div><a href='" + link + "'>" + label + "</a></div>";
        }

    }
