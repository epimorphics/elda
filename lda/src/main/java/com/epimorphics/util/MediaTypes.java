package com.epimorphics.util;

import java.util.HashMap;

public class MediaTypes 
	{
    public static HashMap<String, MediaType> createMediaExtensions()
        {
        HashMap<String, MediaType> result = new HashMap<String, MediaType>();
        result.put( "xml", MediaType.TEXT_XML );
        result.put( "html", MediaType.TEXT_HTML );
        result.put( "text", MediaType.TEXT_PLAIN );
        result.put( "json", MediaType.APPLICATION_JSON );
        result.put( "ttl", MediaType.TEXT_TURTLE );
        result.put( "owl", MediaType.APPLICATION_RDF_XML );
        result.put( "rdf", MediaType.APPLICATION_RDF_XML ); 
        return result;
        }  
    
    public static HashMap<String, String> createNewLanguageExtensions()
	    {
	    HashMap<String, String> result = new HashMap<String, String>();
	    result.put( "en", "en-uk" );
	    return result;
	    }
	}
