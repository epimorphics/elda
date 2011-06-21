/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        MetadataRestlet.java
    Created by:  Dave Reynolds
    Created on:  14 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.ControlRestlet.lookupRequest;
//import static com.epimorphics.lda.restlets.ControlRestlet.lookupRequest;
import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnNotFound;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.CallContext;
import com.epimorphics.lda.restlets.ControlRestlet.SpecRecord;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.epimorphics.vocabs.FIXUP;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Support for viewing the metadata associated with an endpoint.
 * This shows a view of the query that is run and the specification
 * that leads to the query but, unlike the control restlet, doesn't
 * directly link to a dynamically updateable specification.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("/meta/{path: .*}") public class MetadataRestlet {
    
    @GET @Produces("text/plain")
    public Response requestHandlerPlain( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            return returnAs(ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/plain");
        }
    }
    
    @GET @Produces("text/turtle")
    public Response requestHandlerTurtle(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            return returnAs(ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET @Produces("application/json")
    public Response requestHandlerJson(
            @PathParam("path") String pathstub,
            @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            StringWriter writer = new StringWriter();
            List<Resource> roots = new ArrayList<Resource>(1);
            roots.add( meta );
            com.epimorphics.jsonrdf.Context context = rec.getAPIEndpoint().getSpec().getAPISpec().getShortnameService().asContext();
            context.setSorted(true);
            // false == don't want round-trip context in JSON encoding
            Encoder.getForOneResult( context, false ).encodeRecursive(meta.getModel(), roots, writer, true);
            String enc = writer.toString();
            return returnAs(enc, "application/json");
        }
    }

    static final Property SIBLING = ResourceFactory.createProperty( EXTRAS.EXTRA + "SIBLING" );
    
    private Resource createMetadata(UriInfo ui, String pathStub, SpecRecord rec) {
        CallContext cc = CallContext.createContext(ui.getRequestUri(), JerseyUtils.convert(ui.getQueryParameters()), rec.getBindings());
        Model metadata = ModelFactory.createDefaultModel();
        Resource meta = rec.getAPIEndpoint().getMetadata( cc, metadata);
    //
        for (APIEndpointSpec s: rec.getAPIEndpoint().getSpec().getAPISpec().getEndpoints()) {
            String ut = s.getURITemplate().replaceFirst( "^/", "" );
            if (!ut.equals(pathStub)) {
                String sib = ui
                    .getRequestUri()
                    .toASCIIString()
                    .replace( pathStub, ut )
                    .replace( "%7B", "{" )
                    .replace( "%7D", "}" )
                    ;
                meta.addProperty( SIBLING, metadata.createResource( sib ) );
            }
        }
    // Extract the endpoint specification
        Model spec = rec.getSpecModel();
        Resource endpointSpec = rec.getAPIEndpoint().getSpec().getResource().inModel(spec);
        metadata.add( ResourceUtils.reachableClosure( endpointSpec ) );
        meta.getModel().withDefaultMappings( PrefixMapping.Extended );
        meta.addProperty( API.endpoint, endpointSpec );
        return meta;
    }
    
    @GET @Produces("text/html") public Response requestHandlerHTML( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata( ui, pathstub, rec );
            StringBuilder textBody = new StringBuilder();
            h1( textBody, "metadata for " + pathstub );
        //
            List<Statement> sibs = meta.listProperties( SIBLING ).toList();
            if (sibs.size() > 0) {
                h2( textBody, "other endpoints in the same API" );
                for (Statement sib: sibs) {
                    String u = safe( sib.getResource().getURI() );
                    textBody.append( "\n<div class='link'>" );
                    textBody.append( "<a href='" );
                    textBody.append( u );
                    textBody.append( "'>" );
                    textBody.append( u );
                    textBody.append( "</a>" );                    
                    textBody.append( "</div>\n" );
                }
            }
        //
            Statement ep = meta.getProperty( API.sparqlEndpoint );
            h2( textBody, "SPARQL endpoint for queries" );
            textBody
                .append( "<div style='margin-left: 2ex; background-color: #dddddd'>" )
                .append( safe( ep.getResource().getURI() ) )
                .append( "</div>" )
                ;
        //
            StmtIterator comments = meta.listProperties( RDFS.comment );
            if (comments.hasNext()) {
                h2( textBody, "comments on the specification" );
                while (comments.hasNext()) {
                    textBody
                        .append( "\n<div style='margin-left: 2ex; background-color: #dddddd'>\n" )
                        .append( safe( comments.next().getString() ) )
                        .append( "</div>\n" )
                        ;
                }
            }
        //
            StmtIterator queries = meta.listProperties( EXTRAS.sparqlQuery );
            if (queries.hasNext()) {
                h2( textBody, "generated SPARQL query for item selection" );
                while (queries.hasNext()) {
                    textBody.append( "\n<pre style='margin-left: 2ex; background-color: #dddddd'>\n" );
                    doSPARQL( textBody, queries.next().getString() );
                    textBody.append( "\n</pre>\n" );
                }
            }
        //
            h2( textBody, "LDA spec for this endpoint" );
            StringBuilder nice = new StringBuilder();
            StringBuilder prefixes = new StringBuilder();
            renderNicely( prefixes, nice, "", meta, new HashSet<RDFNode>(), 0 );
            textBody.append( "\n<pre>\n" );
            textBody.append( prefixes );
            textBody.append( nice );
            textBody.append( "\n</pre>\n" );
        //
            String it = ModelIOUtils.renderModelAs(meta.getModel(), "Turtle");
            h2( textBody, "LDA spec for this endpoint as raw Turtle" );
            textBody
                .append( "\n<pre style='margin-left: 2ex; background-color: #dddddd'>\n" )
                .append( safe( it ) )
                .append( "\n</pre>\n" )
                ;
            return returnAs( Util.withBody( "Metadata", textBody.toString() ), "text/html" );
        }
    }

    private void doSPARQL( StringBuilder sb, String query ) {
		String [] x = query.split( "\nSELECT " );
		if (x.length == 1) { System.err.println( "OOPS: " + query ); x = new String[] { "", query }; }
		String [] prefixes = x[0].split( "\n" );
		Pattern p = Pattern.compile( "([-A-Za-z]+:)" );
		String pp = "", alt = "";
		Matcher m = p.matcher( x[1] );
		while (m.find()) { pp = pp + alt + "PREFIX[ \\t]+" + m.group(0); alt = "|"; }
		Pattern q = Pattern.compile( pp );
		for (String prefix: prefixes)
			if (q.matcher(prefix).find())
				sb.append( safe( prefix ) ).append( "\n" );
		sb.append( safe( "SELECT " + x[1] ) );
	}

	private void renderNicely( StringBuilder prefixes, StringBuilder sb, String property, RDFNode S, HashSet<RDFNode> seen, int depth ) {
        indent( sb, depth );
        sb.append( property );
        sb.append( " " );
        if (S.isLiteral()) {
            Literal literal = S.asLiteral();
            String lf = literal.getLexicalForm();
            if (lf.indexOf('\n') > -1) {
                sb.append( "\n" );
                for (String line: lf.split("\n" )) {
                    indent( sb, depth + 1 );
                    sb
                        .append( "<span class='literal'>" )
                        .append( safe( line ) )
                        .append( "</span>" )
                        .append( "\n" )
                        ;
                }
            } else {                
                sb.append( "<span class='literal'>" );
                sb.append( safe( lf ) );
                sb.append( "</span>" );                
            }
            String lang = literal.getLanguage();
            String dt = literal.getDatatypeURI();
            if (lang.length() > 0) sb.append( "@" ).append( lang );
            if (dt != null) sb.append( "^^" ).append( dt );
            sb.append( "\n" );
        } else {
            if (S.isAnon()) sb.append( "[] ..." ); else sb.append( nicely(prefixes, S) );
            List<RDFNode> labels = S.asResource().listProperties(FIXUP.label).mapWith(Statement.Util.getObject).toList();
            if (labels.size() > 0) {
                String space = "";
                sb.append( " (" );
                for (RDFNode label: labels) {
                    sb            
                        .append( space )
                        .append( "<span class='literal'>" )
                        .append( safe( label.asLiteral().getLexicalForm() ) )            
                        .append( "</span>" )
                        ;
                    space = " ";
                }
                sb.append( ")" );
            }
            sb.append( "\n" );
            if (seen.add( S )) {
	            List<Statement> properties = S.asResource().listProperties().toList();
	            Collections.sort( properties, byPredicate );
	            for (Statement s: properties) {
	                Property P = s.getPredicate();
	                if (!P.equals(FIXUP.label)  &!P.equals(SIBLING)) {
	                    String p = "<b>" + nicely( prefixes, P ) + "</b>";
	                    renderNicely( prefixes, sb, p, s.getObject(), seen, depth + 1 );
	                }
	            }
            }
        }
    }

    static final Comparator<Statement> byPredicate = new Comparator<Statement>() 
        {
        @Override public int compare( Statement x, Statement y ) 
            { return x.getPredicate().getURI().compareTo( y.getPredicate().getURI() );
            }
        };
        

    private int count = 0;
    
    private String nicely( StringBuilder prefixes, RDFNode S ) {
        Resource r = S.asResource();
        String u = r.getURI();
        Model m = r.getModel();
        String q = m.shortForm( u );
        if (u.equals(q)) {
            String prefix = "p" + ++count, ns = r.getNameSpace();
            m.setNsPrefix( prefix, ns );
            prefixes
                .append( "<span class='keyword'>" )
                .append( "prefix " )
                .append( "</span>" )
                .append( prefix )
                .append( ": &lt;" )
                .append( safe(ns) )
                .append("&gt;\n" )
                ;
            q = m.shortForm( u );
        }
        return safe( q );
    }
    
    private void indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i += 1) sb.append(' ');
    }

    private void h1( StringBuilder textBody, String s ) {  
       textBody.append( "\n<h1>" ).append( safe( s ) ).append( "</h1>" );   
    }
        
    private void h2( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h2>" ).append( safe( s ) ).append( "</h2>\n" );        
    }
    
    private String safe(String val) {
        return val.replaceAll( "&", "&amp;" ).replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}

