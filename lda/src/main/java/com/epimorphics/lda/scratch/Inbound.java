package com.epimorphics.lda.scratch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class Inbound 
	{
	@Test public void uriHandling() throws MalformedURLException
		{
		String template = "http://whatever.com:3000/webapp/command/{argument}";
		String x = "http://whatever.com:3000/webapp/command/1066";
		Map<String, String> values = new HashMap<String, String>();
		URL u = new URL(template);
		String [] segments = u.getPath().split( "/" );
		for (String s: segments) 
			{
			if (s.matches( "\\[A-Za-z]+\\}" )) 
				{
//				values.put(  )
				}
			else
				{ // nothing to do
				
				}
			}
//		System.err.println( ">> " + u.getProtocol() );
//		System.err.println( ">> " + u.getDefaultPort() );
//		System.err.println( ">> " + u.getPort() );
//		System.err.println( ">> " + u.getHost() );
//		System.err.println( ">> " + u.getPath() );
//		System.err.println( ">> " + u.getQuery() );
		}
	}
