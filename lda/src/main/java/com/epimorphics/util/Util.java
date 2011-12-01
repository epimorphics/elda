/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import com.epimorphics.lda.exceptions.EldaException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;

public class Util
    {   
	protected static String htmlWrapper = readResource( "textlike/html-wrapper.html" );
    
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
            .replace( "{$body}", body )
            ;
        }

    /**
        Answer the URI with the given spelling. If there's a syntax error,
        throw a wrapped exception.
    */
	public static URI newURI( String u ) 
		{
		try 
			{ return new URI( u ); }
		catch (URISyntaxException e) 
			{ throw new EldaException( "created a broken URI: " + u, "", EldaException.SERVER_ERROR, e ); }
		} 
	
    public static final class EchoStringReader extends Reader 
    	{
		private final String text;
		int i = 0;
	
		public EchoStringReader(String text) 
			{ this.text = text; }
	
		@Override public void close() throws IOException 
			{}
	
		@Override public int read( char[] dest, int at, int n ) throws IOException 
			{
			if (i == text.length()) return 0;
			dest[at] = text.charAt(i++);
			System.err.print(dest[at] );
			return 1;
			}
    	}
	 
    }
