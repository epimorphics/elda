/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.renderers;

import java.util.*;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class HTMLRenderer implements Renderer {
	
    @Override public MediaType getMediaType( Bindings irrelevant ) {
        return MediaType.TEXT_HTML;
    }

    @Override public String getPreferredSuffix() {
    	return "html";
    }
    
    @Override public Mode getMode() {
    	return Mode.PreferLocalnames;
    }

    @Override public Renderer.BytesOut render( Times t, Bindings ignored, Map<String, String> termBindings, APIResultSet results ) {
    	boolean isItemRendering = results.listStatements( null, API.items, (RDFNode) null ).hasNext() == false;
        return new BytesOutString( isItemRendering ? renderItem(results) : renderList(results) );
    }

	private String renderItem( APIResultSet results ) {
		StringBuilder textBody = new StringBuilder();
        Resource root = results.getRoot().getProperty(FOAF.primaryTopic).getResource();
        h1( textBody, "properties of " + root.getURI() );
        renderResourceDetails( textBody, root );
		return Util.withBody( "description of " + root, textBody.toString() );
	}

	public String renderList(APIResultSet results) {
		StringBuilder textBody = new StringBuilder();
        Resource root = results.getRoot();
        String main = root.getURI() ;
        h1( textBody, "result-set for query " + main );
        Resource anchor = results.listStatements( null, API.items, (RDFNode) null ).next().getSubject();
        for (RDFNode elem: anchor.getProperty( API.items ).getResource().as( RDFList.class ).asJavaList())
            {
            Resource e = (Resource) elem;
            h2( textBody, e.getURI() );
            renderResourceDetails(textBody, e);
            }
        linkyBits( textBody, anchor );
        return Util.withBody( "result set for " + main, textBody.toString() );
	}

	public void renderResourceDetails(StringBuilder textBody, Resource e) {
		List<String> props = new ArrayList<String>();
		for (Statement s: e.listProperties().toList()) { 
		    boolean isAnon = s.getObject().isAnon();
		    String primaryText = brief( "font-weight: bold", s.getPredicate() ) + " " + (isAnon ? "" : brief( s.getObject()) );
		    StringBuilder secondary = new StringBuilder();
		    if (isAnon)
		        {
		        List<String> details = new ArrayList<String>();
		        for (Statement ss: s.getResource().listProperties().toList())
		            details.add( brief( "font-weight: bold", ss.getPredicate() ) + " " + brief( ss.getObject() ) );
		        Collections.sort( details );
		        for (String detail: details)
		        	{
		        	div( secondary, "property-details", "\n" + detail );
		        	}
		        }
		    props.add( primaryText + secondary.toString() );
		}
		Collections.sort(props);
		int count = 0;
		for (String prop : props) 
			{
			count += 1;
			div( textBody, "property-values", prop);
			if (count % 5 == 0) div( textBody, "", "&nbsp;" );
			}
	}

    private void linkyBits( StringBuilder textBody, Resource anchor )
        {
        StringBuilder links = new StringBuilder();
        Statement first = anchor.getProperty( XHV.first );
        Statement next = anchor.getProperty( XHV.next );
        Statement prev = anchor.getProperty( XHV.prev );
        if (first != null) links.append( link_to( "first", first.getResource().getURI() ) );
        if (prev != null) links.append( link_to( "prev", prev.getResource().getURI() ) );
        if (next != null) links.append( link_to( "next", next.getResource().getURI() ) );
        div( textBody, "paging-links", links.toString() );        
        }

    private String link_to( String label, String uri )
        {
        return " <a href='" + uri + "'>" + safe(label) + "</a>";
        }

    private void div( StringBuilder x, String classes, String content )
        {
        x.append( "\n<div class='"  ).append( classes ).append( "'>" ).append( content ).append( "</div>" );
        }

    private String brief( String styles, RDFNode x )
    	{
    	return "<span style='" + styles + "'>" + brief( x ) + "</span>";
    	}
    
    private String brief( RDFNode x )
        {
        return
            x.isAnon() ? x.asNode().getBlankNodeLabel()
            : x.isResource() ? qname( (Resource) x ) 
            : x.asNode().getLiteralLexicalForm()
            ;
        }

    private String qname( Resource x )
        { return x.getModel().shortForm( x.getURI() );
        }

    private void h1( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h1>" ).append( safe( s ) ).append( "</h1>" );        
    }

    private void h2( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h2>" ).append( safe( s ) ).append( "</h2>" );        
    }

    private String safe( String s ) {
        return s.replace( "&", "&amp;" ).replace( "<", "&lt;" );
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
