/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.html.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ws.rs.core.PathSegment;

import com.epimorphics.util.Util;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class MatrixUtils
    {
    public static String matrixFilter( PathSegment c )
        {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : c.getMatrixParameters().entrySet())
            b
                .append( ";" )
                .append( Util.urlEncode( entry.getKey() ) )
                .append( "=" )
                .append( Util.urlEncode( entry.getValue().get( 0 ) ) )
                ;
        return b.toString();
        }
    
    public static String matrixFilterWithout( PathSegment c, String notKey, String notValue )
        {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : c.getMatrixParameters().entrySet())
            {
            String property = entry.getKey(), encoded = Util.urlEncode( property );
            for (String value: entry.getValue())
                {
                if (!same( property, notKey, value, notValue ))
                    b.append( ";" ).append( encoded ).append( "=" ).append( Util.urlEncode( value ) );
                }
            }
        return b.toString();
        }

    private static boolean same( String key, String notKey, String value, String notValue )
        { return key.equals( notKey ) && value.equals( notValue ); }

    public static void pruneCandidatesByProperties( PrefixMapping pm, PathSegment c, Set<Resource> candidates )
        {
        for (Map.Entry<String, List<String>> entry : c.getMatrixParameters().entrySet())
            for (Iterator<Resource> ri = candidates.iterator(); ri.hasNext();)
                if (!matches( pm, ri.next(), entry ))
                    ri.remove();
        }

    private static boolean matches( PrefixMapping pm, Resource r, Entry<String, List<String>> entry )
        {
        List<String> values = entry.getValue();
        Node p = toNode( pm, entry.getKey() );
        for (String v: values)
            if (!r.getModel().getGraph().contains( r.asNode(), p, toNode( pm, Util.urlDecode( v ) ) ) )
                return false;
        return true;
        }

    public static Node toNode( PrefixMapping pm, String s )
        {
        return s.startsWith( "\"" )
            ? Node.createLiteral( s.substring( 1, s.length() - 1 ) )
            : Node.createURI( pm.expandPrefix( s ) )
            ;
        }
    }
