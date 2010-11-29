/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.restlets.support;

import java.util.*;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.container.filter.UriConnegFilter;

public class SDX_URI_ConnegFilter extends UriConnegFilter
    {
    protected static final Map<String, MediaType> mediaExtensions = createMediaExtensions();

    protected static final Map<String, String> languageExtensions = createNewLanguageExtensions();
    
    public SDX_URI_ConnegFilter()
        { super( mediaExtensions, languageExtensions  ); }
    
    private static HashMap<String, String> createNewLanguageExtensions()
        {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put( "en", "en-uk" );
        return result;
        }
    
    private static HashMap<String, MediaType> createMediaExtensions()
        {
        HashMap<String, MediaType> result = new HashMap<String, MediaType>();
        result.put( "json", MediaType.APPLICATION_JSON_TYPE );
        result.put( "html", MediaType.TEXT_HTML_TYPE );
        result.put( "text", MediaType.TEXT_PLAIN_TYPE );
        result.put( "ttl", new MediaType( "text", "turtle" ) );
        result.put( "owl", new MediaType( "application", "rdf+xml" ) );
        result.put( "rdf", new MediaType( "application", "rdf+xml" ) ); 
        return result;
        }
    }

    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
