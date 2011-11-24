/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.support.pageComposition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.View.Type;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specmanager.SpecEntry;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.util.Couple;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
    Compose the display of the API config page.
*/
public class ComposeConfigDisplay {
	
	public String configPageMentioning(List<SpecEntry> entries, String pathstub) {
		StringBuilder textBody = new StringBuilder();
		if (pathstub == null) pathstub = "";
	//
		for (SpecEntry se: entries) {
			Resource root = se.getRoot();
			PrefixMapping pm = se.getSpec().getPrefixMap();
			String shortName = root.getModel().shortForm( root.getURI() );
			String label = getLabelled( root, shortName );
			List<APIEndpointSpec> endpoints = se.getSpec().getEndpoints();
			boolean showThis = occursIn( pathstub, endpoints );
			textBody.append( "<h2>" + label + " <a href='javascript:toggle(\"" + shortName + "\")'>show/hide</a></h2>" );
			textBody.append( "<div id='" + shortName + "' class='" + (showThis ? "show" : "hide") + "'>\n" );
	        renderComments( textBody, root );
	    //
	        Statement ep = root.getProperty( API.sparqlEndpoint );
	        h3( textBody, "SPARQL endpoint for queries" );
	        textBody
	            .append( "<div style='margin-left: 2ex; background-color: #dddddd'>" )
	            .append( safe( ep.getResource().getURI() ) )
	            .append( "</div>" )
	            ;
	    // renderVariables( textBody, "h2", "API variables", rec.getAPIEndpoint().getSpec().getAPISpec().getBindings() );
	        textBody.append( "<h2>endpoints</h2>\n" );
	        Collections.sort( endpoints, sortByEndpointURITemplate );
			for (APIEndpointSpec s: endpoints) renderEndpoint( textBody, pathstub, pm, s );  
	    //
	        renderDictionary( textBody, pm, se.getSpec().getShortnameService() );
	        textBody.append( "</div>\n" );
		}
		
        return textBody.toString();
	}  
    
    private boolean occursIn( String pathstub, List<APIEndpointSpec> endpoints ) {
    	for (APIEndpointSpec es: endpoints)
    		if (matches( pathstub, es.getURITemplate().substring(1) )) return true;
		return false;
	}

	private String getLabelled( Resource root, String ifNone ) {
    	Statement s = root.getProperty( RDFS.label );
    	return s == null ? ifNone : s.getString();
	}

	private void renderVariables( StringBuilder sb, PrefixMapping pm, String tag, String title, Bindings b ) {
    	List<String> names = new ArrayList<String>( b.keySet() );
    	if (names.size() > 0) {
	    	sb.append( "<" + tag + ">" ).append( title ).append( "</" + tag + ">\n" );
	    	Collections.sort( names );
	    	sb.append( "<div class='indent'>\n" );
	    	sb.append( "<table>\n" );
	    	sb.append( "<thead><tr><th>name</th><th>lexical form</th><th>type or language</th></tr></thead>\n" );
			for (String name: names) {
				Value v = b.get( name );
				String lf = v.spelling() == null ? "<i>none</i>" : v.spelling();
				String type =
					v.type().length() > 0 ? pm.shortForm( v.type() )
					: v.lang().length() > 0 ? v.lang()
					: ""
					;
				sb.append( "<tr>" )
					.append( "<td>" ).append( name ).append( "</td>" )
					.append( "<td>" ).append( safe(lf) ).append( "</td>" )
					.append( "<td>" ).append( type ).append( "</td>" )
					.append( "</tr>\n" )
					;
			}
			sb.append( "</table>\n" );
			sb.append( "</div>\n" );
    	}
	}

	private void renderDictionary( StringBuilder sb, PrefixMapping pm, ShortnameService sns ) {
    	String name = "api:shortNameDictionary";
    	List<String> shortNames = preferredShortnamesInOrder( sns );
		sb.append( "<h2>shortname dictionary <a href='javascript:toggle(\"" + name + "\")'>show/hide</a>" ).append( " </h2>\n" );
		sb.append( "<div id='" + name + "' class='hide'>" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>short name</th><th>range (if property)</th><th>qname</th></tr></thead>\n" );
		for (String n: shortNames ) {
			String uri = sns.asContext().getURIfromName( n );
			ContextPropertyInfo cpi = sns.asContext().getPropertyByName( n );
			String range = (cpi == null ? "-" : rangeType( pm, cpi.getType() ) );
			sb.append( "<tr>" )
				.append( "<td>" ).append( n ).append( "</td>" )
				.append( "<td>" ).append( range ).append( "</td>" )
				.append( "<td>" ).append( linkFor( pm, uri ) ).append( "</td>\n" )
				.append( "</tr>\n" )
				;
		}
		sb.append( "</table>" );
		sb.append( "</div>\n" );
	}

	private Object linkFor(PrefixMapping pm, String uri) {
		return "<a href='" + uri + "'>" + pm.shortForm( uri ) + "</a>";
	}

	private List<String> preferredShortnamesInOrder(ShortnameService sns) {
		Set<String> allNames = new HashSet<String>( sns.asContext().allNames() );
		Set<String> toRemove = new HashSet<String>();
		for (String oneName: allNames) {
			String uri = sns.asContext().getURIfromName( oneName );
			String preferred = sns.asContext().getNameForURI( uri );
			if (!oneName.equals(preferred)) toRemove.add( oneName );
		}
		allNames.removeAll( toRemove );
		List<String> names = new ArrayList<String>( allNames );
		Collections.sort( names, String.CASE_INSENSITIVE_ORDER );
		return names;
	}
    
    protected String rangeType( PrefixMapping pm, String uri ) {
    	if (uri == null) return "unspecified";
    	return pm.shortForm( uri );    	
    }

    // TODO: metadataoptions, factories
    // link to parent
    void renderEndpoint( StringBuilder sb, String pathStub, PrefixMapping pm, APIEndpointSpec s ) {
    	Resource ep = s.getResource();
    	Bindings b = s.getBindings();
    	String ut = ep.getProperty( API.uriTemplate ).getString(); 
        ShortnameService sns = s.sns();
    //
    	renderHeader(sb, s, pathStub, ut);
    	renderComments(sb, s.getResource());
    	renderExampleRequestPath(sb, ep);
    	renderSettings(sb, s);    	
    	renderItemTemplate(sb, s);
    	renderAllSelectors( sb, ep );
    	renderVariables(sb, pm, "h3", "variable bindings for this endpoint", b );
    	renderViews(sb, ep, pm, s, sns);
    	sb.append( "</div>\n" );
    }

	private void renderHeader(StringBuilder sb, APIEndpointSpec s, String pathStub, String ut) {
		String name = ut;
    	String kind = s.isListEndpoint() ? "list" : "item";
    	sb.append( "<div style='font-size: 150%; margin-top: 1ex'>" )
    		.append( " <a href='javascript:toggle(\"" + name + "\")'>" ).append( name ).append( "</a>" )
    		.append( " [" ).append( kind ).append( " endpoint] " )
    		.append( " </div>\n" )
    		;
    	String visibility = matches( pathStub, ut.substring(1) ) ? "show" : "hide";
    	sb.append( "<div id='" + name + "' class='" + visibility + "'>" );
	}

	private boolean matches( String actual, String pattern ) {
		return actual.matches( pattern.replaceAll( "\\{[^}/]*\\}", "[^/]*" ) );
	}

	private void renderComments(StringBuilder sb, Resource spec) {
		List<Statement> commentStatements = spec.listProperties( RDFS.comment ).toList();
    	if (commentStatements.size() > 0) {
    		sb.append( "<h3>comments</h3>\n" );
    		for (Statement cs: commentStatements) {
    			sb.append( "<p>\n" );
    			sb.append( safe( cs.getString() ) );
    			sb.append( "</p>\n" );
    		}
    	}
	}

	private void renderExampleRequestPath(StringBuilder sb, Resource ep) {
		Property API_exampleRequestPath = ep.getModel().createProperty( API.NS, "exampleRequestPath" );
    	List<Statement> examples = ep.listProperties( API_exampleRequestPath ).toList();
    	if (examples.size() > 0) {
    		sb.append( "<h3>example request path(s)" );
    		for (Statement exs: examples) {
    			sb.append( "<div class='indent'>" ).append( safe( exs.getString() ) ).append( "</div>\n" );
    		}
    	}
	}

	private void renderSettings(StringBuilder sb, APIEndpointSpec s) {
		String dl = s.getDefaultLanguage();
    	sb.append( "<h3>settings</h3>\n" );
    	sb.append( "<div class='indent'>" );
    	sb.append( "default page size: " ).append( s.getDefaultPageSize() );
    	sb.append( ", max page size: " ).append( s.getMaxPageSize() );
    	if (dl != null ) sb.append( ", default languages: " ).append( dl );
    	sb.append( ".\n</div>\n" );
	}

	private void renderItemTemplate(StringBuilder sb, APIEndpointSpec s) {
		String it = s.getItemTemplate();
    	if (it != null) {
    		sb.append( "<h3>item template</h3>\n" );
    		sb.append( "<div class='indent'>" ).append( safe( it ) ).append( "</div>\n" );
    	}
	}

	private void renderViews(StringBuilder sb, Resource ep,  PrefixMapping pm, APIEndpointSpec spec, ShortnameService sns) {
		Set<String> viewNames = spec.getExplicitViewNames();
		View dv = spec.getDefaultView();
		boolean seenDefault = false;
		sb.append( "<h3>views</h3>\n" );
		for (String vn: viewNames) {
			View v = spec.getView( vn );
			boolean isDefault = v.equals( dv );
			renderView(sb, sns, pm, isDefault, v);
			if (isDefault) seenDefault = true;
		}
		if (dv != null && !seenDefault) renderView( sb, sns, pm, true, dv);
	}

	private void renderView(StringBuilder sb, ShortnameService sns, PrefixMapping pm, boolean isDefault, View v) {
		String vn = v.name();
		String type = typeAsString( v.getType() );
		sb.append( "<div style='margin-top: 1ex' class='indent'>\n" );
		sb.append( "<div>" );
		if (isDefault) sb.append( "<b>default</b> " );
		sb.append( "<i style='color: red'>" ).append( vn ).append( "</i>" ).append( type );
		sb.append( "</div>" );
		List<PropertyChain> chains = new ArrayList<PropertyChain>( v.chains() );
		List<String> stringedChains = new ArrayList<String>(chains.size());
		for (PropertyChain pc: chains) stringedChains.add( chainToString( sns, pm, pc ) );
		Collections.sort( stringedChains );
		sb.append( "<div class='indent'>\n" );
		for (String pc: stringedChains) {
			sb.append( "<div>").append( pc ).append( "</div>\n" );
		}
		sb.append( "</div>\n" );
		sb.append( "</div>\n" );
	}

	private String typeAsString(Type type) {
		switch (type) {
			case T_ALL: return " (DESCRIBE + labels)";
			case T_DESCRIBE: return " (DESCRIBE)";
			case T_CHAINS: return " (just these properties)";
		}
		return "IMPOSSIBLE";
	}

	private String chainToString( ShortnameService sns, PrefixMapping pm, PropertyChain pc ) {
		StringBuilder sb = new StringBuilder();
		List<Property> properties = pc.getProperties();
		Property last = properties.get( properties.size() - 1 );
		String dot = "";
		for (Property p: properties) {
			sb.append( dot ).append( shortForm( sns, p ) );
			dot = ".";
		}
		ContextPropertyInfo cpi = sns.asContext().findProperty( last );
		if (cpi != null) {
			String type = cpi.getType();
			if (type != null && type.startsWith( "http:" )) {
				String shortName = rangeType( pm, type );
				sb.append( " <i style='color: blue'>" ).append( shortName ).append( "</i>" );
			}
		}
		return sb.toString();
	}

	private void renderAllSelectors( StringBuilder sb, Resource ep ) {
		List<Statement> selectors = ep.listProperties( API.selector ).toList();
		if (selectors.size() > 0) {
			sb.append( "<h3>selectors</h3>\n" );
			for (Statement selector: selectors) 
				renderSelectors(sb, selector.getResource() );
				
		}
	}

	private void renderSelectors( StringBuilder sb, Resource sel ) {
		Set<Couple<String, Resource>> filters = allFiltersOf( new HashSet<Couple<String, Resource>>(), sel );
		for (Couple<String, Resource> filter: filters) {
			String from = (filter.b.equals( sel ) ? "" : " (from " + shortForm( filter.b ) + ")");
			sb.append( "<div class='indent'>" )
				.append( "<b>filter</b> " )
				.append( filter.a )
				.append( from )
				.append( "</div>\n" )
				;
		}
		List<Statement> wheres = sel.listProperties( API.where ).toList();
		for (Statement where: wheres) {
			sb.append( "<div class='indent'>" )
				.append( "<b>where</b>\n" )
				.append( "<pre>" )
				.append( where.getString() )
				.append( "</pre>\n" )
				.append( "</div>\n" )
				;			
		}
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

    static final Comparator<APIEndpointSpec> sortByEndpointURITemplate = new Comparator<APIEndpointSpec>() {

		@Override public int compare( APIEndpointSpec ep1, APIEndpointSpec ep2 ) {
			return ep1.getURITemplate().compareTo( ep2.getURITemplate() );
		}
    };
    
    static final Comparator<Statement> sortByStatementObjectResource = new Comparator<Statement> () {

		@Override public int compare( Statement a, Statement b ) {
			return a.getResource().getURI().compareTo( b.getResource().getURI() );
		}
    };
    
    protected static String shortForm( ShortnameService sns, Resource r ) {
    	String x = sns.asContext().getNameForURI( r.getURI() );
    	return x == null ? shortForm( r ) : x;
    }

	protected static String shortForm( Resource r ) {
		return r.getModel().shortForm( r.getURI() );
	}
        
    private void h3( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h3>" ).append( safe( s ) ).append( "</h3>\n" );        
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
