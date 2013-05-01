/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import java.io.InputStream;

import com.epimorphics.lda.exceptions.EldaException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

public class Util
    {   
	
	protected static final String style = readResource( "textlike/style.css" );
	
	protected static final String htmlWrapper = readResource( "textlike/html-wrapper.html" );
    	
    public static String readResource( String path )
        {
        InputStream in = Util.class.getClassLoader().getResourceAsStream( path );
        if (in == null) EldaException.NotFound( "resource", path );
        return FileManager.get().readWholeFileAsUTF8( in );
        }
    
    public static Model readModel( String path )
        {
        InputStream in = Util.class.getClassLoader().getResourceAsStream( path );
        if (in == null) EldaException.NotFound( "model", path );
        return ModelFactory.createDefaultModel().read( in, "", "TTL" );
        }
    
    public static String withBody( String title, String body )
        {
        return htmlWrapper
            .replace( "{$title}", title )
            .replace( "{$style}", "<style>\n" + style + "</style>\n" )
            .replace( "{$body}", body )
            ;
        }	    
}
