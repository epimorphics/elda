/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.restful.support;

import java.io.*;
import java.util.*;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import com.epimorphics.html.util.MatrixUtils;
import com.epimorphics.html.util.SDX_Utils;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.restful.restlets.Catalogue;
import com.epimorphics.sdx.vocabulary.DSV;
import com.epimorphics.util.Util;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class CatalogueInJSON
    {
    private final SharedConfig config;
    
    public CatalogueInJSON( SharedConfig config )
        { this.config = config; }
    
    public String modelToJSONString( Model m )
        {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeModel( m, writerFromStream( bos ) );
        return bos.toString();
        }
    
    private final Model ontology = consOntology();
    
    private void writeModel( Model m, Writer w )
        {
        try { Encoder.get( ontology ).encode( m, null, w, true ); /* m.write( w, "TTL" ); */ w.flush(); }
        catch (IOException e) { throw new WrappedIOException( e ); }
        }
    
    private Model consOntology()
        {
        Model result = ModelFactory.createDefaultModel();
        return result;
        }

    private Writer writerFromStream( ByteArrayOutputStream bos )
        {
        try { return new OutputStreamWriter( bos, "UTF-8" ); }
        catch (UnsupportedEncodingException e) { throw new WrappedException( e ); }
        }
    
    public Model catalogueModelForCandidates( Resource root, PathSegment c, Set<Resource> candidates )
        {
        return ModelFactory.createDefaultModel()
            .add( root, RDF.type, DSV.Catalogue )
            .add( root, DSV.hasURL, root )
            .add( statementsForEntries( root, candidates ) )
            .add( statementsForFilters( root, c ) )
            .add( statementsForProperties( root, c, candidates ) )
            ;        
        }

    private List<Statement> statementsForEntries( Resource root, Set<Resource> candidates )
        {
        List<Statement> result = new ArrayList<Statement>();
        for (Resource c: candidates)
            {
            Model m = c.getModel();
            String id = config.pathFor( "datasets", SDX_Utils.idOf( c ) );
            Resource rid = m.createResource( id );
            result.add( m.createStatement( root, DSV.hasEntry, rid ) );
            result.add( m.createStatement( rid, RDF.type, DSV.DataSet ) );
            result.add( m.createStatement( rid, RDFS.label, SDX_Utils.labelOf( c ) ) );
            }
        return result;
        }
    
    private List<Statement> statementsForProperties( Resource root, PathSegment c, Set<Resource> candidates )
        {
        Model m = root.getModel();
        String matrix = MatrixUtils.matrixFilter( c );
        ArrayList<Statement> result = new ArrayList<Statement>();
        Set<Property> properties = Catalogue.getAllProperties( candidates );
        for (Property p: properties)
            {
            Resource property = m.createResource();
            String linkURI = config.pathFor( "anchor" + matrix,  "property;p=" + Util.urlEncode( Util.shortForm( p ) ) );
            Resource link = m.createResource( linkURI );
            result.add( m.createStatement( root, DSV.hasProperty, property ) );
            result.add( m.createStatement( property, DSV.propertyName, p ) );
            result.add( m.createStatement( property, DSV.valuesFor, link  ) );
            }
        return result;
        }

    private List<Statement> statementsForFilters( Resource root, PathSegment c )
        {
        Model m = root.getModel();
        ArrayList<Statement> result = new ArrayList<Statement>();
        MultivaluedMap<String, String> map = c.getMatrixParameters();
        for (Map.Entry<String, List<String>> entry : map.entrySet())
            for (String value: entry.getValue())
                {
                Resource filter = m.createResource();
                String p = entry.getKey();
                RDFNode v = nodeFor( value );
                Resource wider = m.createResource( config.pathFor( "catalogue" + MatrixUtils.matrixFilterWithout( c, p, value ) ) );
                result.add( m.createStatement( root, DSV.hasFilter, filter ) );
                result.add( m.createStatement( filter, DSV.onProperty, p ) );
                result.add( m.createStatement( filter, DSV.hasValue, v ) );
                result.add( m.createStatement( filter, DSV.widerCatalogue, wider ) );
                }
        return result;
        }

    private RDFNode nodeFor( String value )
        {
        Node n = MatrixUtils.toNode( PrefixMapping.Extended, value );
        if (n.isURI()) return ResourceFactory.createResource( n.getURI() );
        if (n.isLiteral()) return ResourceFactory.createPlainLiteral( n.getLiteralLexicalForm() );
        throw new UnsupportedOperationException( value );
        }
    }
