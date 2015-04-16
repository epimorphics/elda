/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import java.io.InputStream;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.support.EldaFileManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class Util
    {   
	
	protected static final String style = readResource( "textlike/style.css" );
	
	protected static final String htmlWrapper = readResource( "textlike/html-wrapper.html" );
    	
    public static String readResource( String path )
        {
        InputStream in = Util.class.getClassLoader().getResourceAsStream( path );
        if (in == null) EldaException.NotFound( "resource", path );
        return EldaFileManager.get().readWholeFileAsUTF8( in );
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
