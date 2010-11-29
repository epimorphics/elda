/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.util;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Util
    {
    public static String withBody( String body )
        { return withBody( "", body ); }
    
    protected static String htmlWrapper = readResource( "textlike/html-wrapper.html" );
    
    public static String readResource( String path )
        {
        InputStream in = Util.class.getClassLoader().getResourceAsStream( path );
        if (in == null) throw new NotFoundException( path );
        return FileManager.get().readWholeFileAsUTF8( in );
        }
    
    public static Model readModel( String path )
        {
        InputStream in = Util.class.getClassLoader().getResourceAsStream( path );
        if (in == null) throw new NotFoundException( path );
        return ModelFactory.createDefaultModel().read( in, "", "TTL" );
        }
    
    public static String withBody( String title, String body )
        {
        return htmlWrapper
            .replace( "{$title}", title )
            .replace( "{$body}", body )
            ;
        }

    public static String urlEncode( String s )
        {
        try { return URLEncoder.encode( s, "UTF-8" ); }
        catch (UnsupportedEncodingException e) { throw new WrappedException( e ); }
        }
    
    public static String urlDecode( String s )
        {
        try { return URLDecoder.decode( s, "UTF-8" ); }
        catch (UnsupportedEncodingException e) { throw new WrappedException( e ); }
        }
    
    public static String shortForm( Resource p )
        {
        try { return p.getModel().shortForm( p.getURI() ); }
        catch (NullPointerException e) { return p.getURI(); } // HACK -- somehow null values are getting into the pm
        }

    private static String shortForm( RDFNode r, Node n )
        {
        try { return ((Resource) r).getModel().shortForm( n.getURI() ); }
        catch (NullPointerException e) { return n.getURI(); } // HACK -- somehow null values are getting into the pm
        }

    public static String niceName( Resource p )
        {
        List<Statement> labels = p.listProperties( RDFS.label ).toList();
        return labels.isEmpty() ? Util.shortForm( p ) : labels.get( 0 ).getString();
        }

    public static String urlEncodeNode( RDFNode v )
        { return urlEncode( encodeNode( v ) ); }

    private static String encodeNode( RDFNode r )
        {
        Node n = r.asNode();
        if (n.isURI()) return shortForm( r, n );
        if (n.isLiteral()) return encodeLiteral( n );
        throw new UnsupportedOperationException( "not supported: " + r );
        }

    private static String encodeLiteral( Node lit )
        {
        return "\"" + lit.getLiteralLexicalForm() + "\"";
        }        
    }
