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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.RDFUtil;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.restlets.ControlRestlet.SpecRecord;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.PrefixLogger;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.Couple;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDF;
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
    public Response requestHandlerTurtle( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            return returnAs(ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET @Produces("application/json")
    public Response requestHandlerJson( @PathParam("path") String pathstub, @Context UriInfo ui) {
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
        Bindings cc = Bindings.createContext( rec.getBindings(), JerseyUtils.convert( ui.getQueryParameters() ) );
        Model metadata = ModelFactory.createDefaultModel();
        Resource meta = rec.getAPIEndpoint().getMetadata( cc, ui.getRequestUri(), metadata);
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
        metadata.setNsPrefixes( spec );
        metadata.add( ResourceUtils.reachableClosure( endpointSpec ) );
        meta.getModel().withDefaultMappings( PrefixMapping.Extended );
        meta.addProperty( API.endpoint, endpointSpec );
        return meta;
    }
    
    Comparator<Statement> sortByStatementObjectResource = new Comparator<Statement> () {

		@Override public int compare( Statement a, Statement b ) {
			return a.getResource().getURI().compareTo( b.getResource().getURI() );
		}
    	
    };
    
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
            	Collections.sort( sibs, sortByStatementObjectResource );
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
            renderVariables( textBody, "h2", "API variables", rec.getAPIEndpoint().getSpec().getAPISpec().getBindings() );
        //
            StringBuilder specBuilder = new StringBuilder();
            List<APIEndpointSpec> endpoints = rec.getAPIEndpoint().getSpec().getAPISpec().getEndpoints();
            Collections.sort( endpoints, sortByEndpointResource );
			for (APIEndpointSpec s: endpoints) renderEndpoint( specBuilder, ui, s );  
            textBody.append( specBuilder.toString() );
        //
            renderDictionary( textBody, meta.getModel(), rec.getAPIEndpoint().getSpec().getAPISpec().getShortnameService() );
        //
            return returnAs( Util.withBody( "Metadata", textBody.toString() ), "text/html" );
        }
    }
    
    private void renderVariables( StringBuilder sb, String tag, String title, Bindings b ) {
    	List<String> names = new ArrayList<String>( b.keySet() );
    	if (names.size() > 0) {
	    	sb.append( "<" + tag + ">" ).append( title ).append( "</" + tag + ">\n" );
	    	PrefixLogger pl = new PrefixLogger();
	    	Collections.sort( names );
	    	sb.append( "<table>\n" );
			for (String name: names) {
				Value v = b.get( name );
				sb.append( "<tr>" )
					.append( "<td>" ).append( name ).append( "</td>" )
					.append( "<td>" ).append( v.asSparqlTerm(pl) ).append( "</td>" )
					.append( "</tr>\n" )
					;
			}
			sb.append( "</table>\n" );
    	}
	}

	private void renderDictionary( StringBuilder sb, PrefixMapping pm, ShortnameService sns ) {
    	String name = "api:shortNameDoctionary";
		sb.append( "<h2>Dictionary <a href='javascript:toggle(\"" + name + "\")'>&nabla; show/hide</a>" ).append( " </h2>\n" );
		sb.append( "<div id='" + name + "' class='hide'>" );
		List<String> names = new ArrayList<String>( sns.asContext().allNames() );
		Collections.sort( names, String.CASE_INSENSITIVE_ORDER );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>short name</th><th>range (if property)</th><th>qname</th></tr></thead>\n" );
		for (String n: names ) {
			String uri = sns.asContext().getURIfromName( n );
			String sf = pm.shortForm( uri );
			ContextPropertyInfo cpi = sns.asContext().getPropertyByName( n );
			String range = (cpi == null ? "-" : rangeType( pm, cpi.getType() ) );
			sb.append( "<tr class='zebra'>" )
				.append( "<td>" ).append( n ).append( "</td>" )
				.append( "<td>" ).append( range ).append( "</td>" )
				.append( "<td>" ).append( sf ).append( "</td>\n" )
				.append( "<tr>\n" )
				;
		}
		sb.append( "</table>" );
		sb.append( "</div>\n" );
	}
    
    protected String rangeType( PrefixMapping pm, String uri ) {
    	if (uri == null) return "unspecified";
    	return pm.shortForm( uri );    	
    }

    // TODO: default language, page size, maxPageSioze, itemTemplate 
    // TODO: metadataoptions, factories
    // link to parent
    void renderEndpoint( StringBuilder sb, UriInfo ui, APIEndpointSpec s ) {
    	Resource ep = s.getResource();
    	Bindings b = s.getBindings();
    	String ut = ep.getProperty( API.uriTemplate ).getString(); 
    	SpecRecord rec = lookupRequest( safe(ut.substring(1)), ui );
        Resource meta = createMetadata( ui, ut, rec );
        Statement q = meta.getProperty(EXTRAS.sparqlQuery );
    //
    	String name = shortForm( ep ); 
    	String kind = s.isListEndpoint() ? "list" : "item";
    	sb.append( "<h2>" )
    		.append( kind ).append( " endpoint " ).append( name )
    		.append( " <a href='javascript:toggle(\"" + name + "\")'>&nabla; show/hide</a>" )
    		.append( " </h2>\n" )
    		;
    	sb.append( "<div id='" + name + "' class='hide'>" );
    	
    //
    	String dl = s.getDefaultLanguage();
    	sb.append( "<h3>settings</h3>\n" );
    	sb.append( "<div class='indent'>" );
    	sb.append( "default page size: " ).append( s.getDefaultPageSize() );
    	sb.append( ", max page size: " ).append( s.getMaxPageSize() );
    	if (dl != null ) sb.append( ", default languages: " ).append( dl );
    	sb.append( ".\n</div>\n" );    	
    //
    	sb.append( "<h3>uri template</h3>\n" );
    	sb.append( "<div class='indent'>" ).append( ut ).append( "</div>\n" );
    //
    	String it = s.getItemTemplate();
    	if (it != null) {
    		sb.append( "<h3>item template</h3>\n" );
    		sb.append( "<div class='indent'>" ).append( safe( it ) ).append( "</div>\n" );
    	}
    //
    	Resource sel = ep.getProperty( API.selector ).getResource();
    	if (sel != null) renderSelectors(sb, sel, q.getString() );
    //
    	renderVariables(sb, "h3", "endpoint variables", b );
    //
    	sb.append( "<h3>views</h3>\n" );
    	Statement dv = ep.getProperty( API.defaultViewer );
    	if (dv != null) showView( sb, dv.getObject(), true );
    	RDFNode dvo = dv == null ? null : dv.getObject();
    	for (RDFNode viewer: ep.listProperties( API.viewer ).mapWith( Statement.Util.getObject ).toSet()) {
    		if (!viewer.equals( dvo )) showView( sb, viewer, false );
    	}
    	sb.append( "</div>\n" );
    }

	private void renderSelectors(StringBuilder sb, Resource sel, String query) {
		sb.append( "<h3>selectors</h3>\n" );
		Set<Couple<String, Resource>> filters = allFiltersOf( new HashSet<Couple<String, Resource>>(), sel );
		for (Couple<String, Resource> filter: filters) {
			String from = (filter.b.equals( sel ) ? "" : " (from " + shortForm( filter.b ) + ")");
			sb.append( "<div class='indent'>" )
				.append( "<b>" ).append( "filter " ).append( "</b>" )
				.append( filter.a )
				.append( from )
				.append( "</div>\n" )
				;
		}
		Statement where = sel.getProperty( API.where );
		if (where != null)
			sb.append( "  " ).append( "where " ).append( where.getString() ).append( "\n" );
		sb.append( "<h4>generated query</h4> " );
        sb.append( "\n<pre style='margin-left: 2ex; background-color: #dddddd'>\n" );
		doSPARQL( sb, query );
		sb.append( "</pre>\n" );
	}

	private void showView(StringBuilder sb, RDFNode viewer, boolean isDefault) {
		Resource v = (Resource) viewer;
		String u = v.getURI();
		String viewName = RDFUtil.getStringValue( v, API.name );
		if (viewName == null) viewName = "";
		if (u == null) u = "anonymous";
		sb.append( "<div class='indent'>" );
		sb
			.append( isDefault ? "<b>default</b> " : "" )
			.append( "<i style='color: red'>" ).append( viewName ).append( "</i>" )
			.append( " " ).append( v.getModel().shortForm( u ) )
			.append( "\n" )
			;
		for (Statement s : v.listProperties( API.property ).toList()) {
			sb.append( "<div class='indent'>" ).append( propertyChain( s.getObject() ) ).append( "</div>\n" );
		}
		sb.append( "\n</div>\n" );
	}
    
    protected Set<Couple<String, Resource>> allFiltersOf( Set<Couple<String, Resource>> them, Resource sel) {
    	for (RDFNode p: sel.listProperties( API.parent ).mapWith( Statement.Util.getObject ).toList()) {
    		allFiltersOf( them, (Resource) p );
    	}
    	for (RDFNode f: sel.listProperties( API.filter ).mapWith( Statement.Util.getObject).toList()) {
    		String pvs = ((Literal) f).getLexicalForm();
    		for (String filter: pvs.split( "&" ))
    			them.add( new Couple<String, Resource>( filter, sel ) );
    	}
    	return them;
	}

    protected String propertyChain( RDFNode node ) {
    	if (RDFUtil.isList(node)) return propertyList( (Resource) node );
    	if (node.isResource()) return shortForm( (Resource) node );
    	if (node.isLiteral()) return ((Literal) node).getLexicalForm();
    	return "Unexpected property chain '" + node + "'";
    }

    protected String propertyList(Resource node) {
    	String result = "";
    	for (RDFNode p: node.as(RDFList.class).asJavaList()) {
    		result = result + " " + shortForm( (Resource) p );
    	}
    	return result;
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

    static final Comparator<Statement> byPredicate = new Comparator<Statement>() 
        {
        @Override public int compare( Statement x, Statement y ) 
            { return x.getPredicate().getURI().compareTo( y.getPredicate().getURI() );
            }
        };

    static final Comparator<APIEndpointSpec> sortByEndpointResource = new Comparator<APIEndpointSpec>() {

		@Override public int compare( APIEndpointSpec ep1, APIEndpointSpec ep2 ) {
			return shortForm(ep1.getResource() ).compareTo( shortForm( ep2.getResource() ) );
		}
    	
    };

	protected static String shortForm( Resource r ) {
		return r.getModel().shortForm( r.getURI() );
	}
	
    private void h1( StringBuilder textBody, String s ) {  
       textBody.append( "\n<h1>" ).append( safe( s ) ).append( "</h1>" );   
    }
        
    private void h2( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h2>" ).append( safe( s ) ).append( "</h2>\n" );        
    }
    
    private String safe(String val) {
        return 
        	val.replaceAll( "&", "&amp;" )
        	.replaceAll("<", "&lt;")
        	.replaceAll(">", "&gt;")
        	.replaceAll( "[{]([A-Za-z0-9_]+)[}]", "a_$1" )
        	;
    }
}

