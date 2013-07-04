/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/
package com.epimorphics.lda.demo;

import java.util.*;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.BytesOutString;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.XHV;
import com.epimorphics.util.*;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Demo_HTML_Renderer implements Renderer {

	protected final APIEndpoint endpoint;
	protected final ShortnameService sns;
	
	protected static final FileManager fm = new FileManager(EldaFileManager.get());
	
	{ fm.setModelCaching( true ); }
	
    public Demo_HTML_Renderer( APIEndpoint ep, ShortnameService sns ) {
    	this.sns = sns;
    	this.endpoint = ep;
    }

    @Override public String getPreferredSuffix() {
    	return "html";
    }
    
	@Override public MediaType getMediaType( Bindings irrelevant ) {
        return MediaType.TEXT_HTML;
    }
    
    @Override public CompleteContext.Mode getMode() {
    	return CompleteContext.Mode.PreferLocalnames;
    }

    @Override public Renderer.BytesOut render( Times t, Bindings ignored, Map<String, String> termBindings, APIResultSet results ) {
    	handleFakeBnodes( results.getMergedModel() );
    	boolean isItemRendering = results.listStatements( null, API.items, (RDFNode) null ).hasNext() == false;
        return new BytesOutString( isItemRendering ? renderItem(results) : renderList(results) );
    }
    
    private String shorten( String uri ) {
    	return sns.asContext().getNameForURI( uri );
    }

	private void handleFakeBnodes(Model m) {
		List<RDFNode> nodes = m.listObjects().filterKeep( fakeBNode ).toList();
		for (RDFNode n: nodes) m.add( modelFor(n) );
	}

	private Model modelFor( RDFNode n ) {
		return fm.loadModel( n.asNode().getURI() );
	}
	
	static final Filter<RDFNode> fakeBNode = new Filter<RDFNode>() 
		{
		@Override public boolean accept(RDFNode o) 
			{
			return o.isURIResource() && o.asNode().getURI().matches( "http://api.talis.com/stores/.*#self" );
			}
		};

	private String renderItem( APIResultSet results ) {
		StringBuilder textBody = new StringBuilder();
        Resource root = results.getRoot();
        String x = root.getURI().replaceAll( "\\.html", "" ).replaceFirst( "[?&].*", "" );
        h1( textBody, "properties of " + root.getURI() );
        renderResourceDetails( textBody, x, root );
		return Util.withBody( "description of " + root, textBody.toString() );
	}

	public String renderList( APIResultSet results ) {
		StringBuilder textBody = new StringBuilder();
        String rootURI = results.getRoot().getURI();
        h1( textBody, "Elda query results" );
        renderParameters(textBody, rootURI);
        renderMetadata( textBody, results.getMergedModel() );
    //    
        Resource anchor = results.listStatements( null, API.items, (RDFNode) null ).next().getSubject();
        for (RDFNode elem: anchor.getProperty( API.items ).getResource().as( RDFList.class ).asJavaList())
            {
        	textBody.append( "\n<div class='one-item'>\n" );
            Resource e = (Resource) elem;
            String name = getSubjectName( e );
            h2( textBody, name );
            renderResourceDetails(textBody, rootURI, e);
            textBody.append( "\n</div>" );
            }
        linkyBits( textBody, anchor );
        return Util.withBody( "Elda result set", textBody.toString() );
	}

	private void renderMetadata( StringBuilder textBody, Model rsm ) {
		StmtIterator sit = rsm.listStatements( null, API.definition, (RDFNode) null );
		if (sit.hasNext()) {
			String def = sit.next().getResource().getURI();
			textBody
				.append( "<div style='margin: 1ex'>" )
				.append( "<b>definition</b> at: " )
				.append( "<a href='" + def + "'>" + def + "</a>" )
				.append( "</div>" )
				;
		}
	}

	private void renderParameters( StringBuilder textBody, String main ) 
		{
		String [] parts = main.split( "[&?]");
        parts[0] = "Request=" + parts[0];
        h2(textBody, "request parameters:" );
        textBody.append( "<table class='zebra'>\n" );
        for (String part: parts)
        	{
        	String [] pv = part.split("="); 
        	if (pv.length == 1) pv = new String[] {pv[0], "UNKNOWN"};
        	textBody
        		.append( "<tr>" )
        		.append( "<td align='right'>" ).append( pv[0] ).append( ": </td>" )
        		.append( "<td>" ).append( pv[1] ).append( "</td>" )
        		.append( "</tr>" )
        		;
        	}
        textBody.append( "</table>\n" );
		}

	private String getSubjectName( Resource e ) 
		{
		return 
			e.hasProperty( SCHOOL.establishmentName) ? e.getProperty(SCHOOL.establishmentName).getString() 
			: e.hasProperty(RDFS.label) ? e.getProperty(RDFS.label).getString() 
			: "School #" + e.getURI().replaceFirst( ".*[/#]", "" )
			;
		}

	Comparator<Triad<String, String, String>> compareStringTriad = new Comparator<Triad<String, String, String>>() 
		{
		@Override public int compare(Triad<String, String, String> x, Triad<String, String, String> y) 
			{
			int result = x.a.compareTo(y.a);
			if (result == 0) result = x.b.compareTo(y.b);
			if (result == 0) result = x.c.compareTo(y.c);
			return result;
			}
		};
	
	public void renderResourceDetails(StringBuilder textBody, String x, Resource e) 
		{
		List<Triad<String, String, String>> props = new ArrayList<Triad<String, String, String>>();
		for (Statement s: e.listProperties().toList()) {
		    Property p = s.getPredicate();
		    String value = makeEntry(x, s, p, brief( s.getObject() ));
		    String shortP = shorten(p.getURI());
		    String title = "click to try and get definition details for " + shortP;
			String pd =	"<a href='" + p.getURI() + "' title='" + title + "'>" + shortP + "</a>";
		    props.add( new Triad<String, String, String>( "", pd, value ) );
		}
		Collections.sort( props, compareStringTriad );
	//
		textBody
			.append( "<table class='zebra' style='margin-left: 2ex'>" )
			.append( "<thead>" )
			.append( "<tr><th width='2em'></th><th width='18%'></th><th width='78%'></th></tr>" )
			.append( "</thead>" )
			;
		int count = 0;
		for (Triad<String, String, String> prop : props) 
			{
			count += 1;
			textBody.append( "<tr>" )
				.append( "<td>" ).append( prop.a ).append( "</td>" )
				.append( "<td align='right'>" ).append( prop.b ).append( "</td>" )
				.append( "<td>" ).append( prop.c ).append( "</td>" )
				.append( "</tr>" )
				.append( "\n" )
				;
			}
		textBody.append( "</table>" );
	}
	
	private String makeEntry( String x, Statement s, Property p, String value )
		{			
		RDFNode ob = s.getObject();
	    boolean isAnon = 
	    	ob.isAnon() 
	    	|| ob.isResource() && ob.asResource().getURI().matches( "http://api.talis.com/stores/.*#self" )
	    	;
	    if (isAnon) //  && p.getModel().listStatements( ob.asResource(), null, (RDFNode) null ).toList().size() > 0)
	    	{
	    	StringBuilder vv = new StringBuilder();
	    	vv.append( "<span>\n" );
	    	String gap = "";
	    	List<Statement> sts = sortBypredicate( ob.asResource().listProperties().toList() );
	    	for (Statement st: sts)
	    		{
	    		vv.append( gap );
	    		vv.append( shortPropertyName(st) );
	    		vv.append( ": " );
	    		vv.append( brief( st.getObject() ) );
	    		gap = "; ";
	    		}
	    	vv.append( "</span>\n" );
	    	return vv.toString();
	    	}
	    else if (ob.isResource())
			{
			Resource o = ob.asResource();
			Statement label = o.getProperty(RDFS.label);
			if (label != null) value = protect(label.getString()); 
			value = value + " " + resRequest( x, p, o );
			}
		else if (ob.isLiteral()) 
			{
			String u = ob.asLiteral().getDatatypeURI();
			if (u != null)
		    	{
		    	if (u.endsWith("#integer") || u.endsWith("#date"))
		    		{
		    		value = 
		    			intRequest(x, Mode.MAX, p, value) 
		    			+ " " + intRequest( x, Mode.EQ, p, value ) 
		    			+ " " + intRequest( x, Mode.MIN, p, value )
		    			;
		    		}
		    	}
			}
		return value;
		}

	static final Comparator<Statement> byPredicate = new Comparator<Statement>() 
		{
		@Override public int compare( Statement x, Statement y ) 
			{ return x.getPredicate().getURI().compareTo( y.getPredicate().getURI() );
			}
		};
		
	private List<Statement> sortBypredicate(List<Statement> list) 
		{
		Collections.sort( list, byPredicate );
		return list;
		}

	private String shortPropertyName( Statement st ) 
		{
		String uri = st.getPredicate().getURI();
		String shorter = shorten( uri );
		return shorter == null ? st.getModel().shortForm( uri ) : shorter;
		}
	
	static class Mode 
		{ 
		final String related;
		final String prefix;
		
		public Mode( String related, String prefix )
			{ this.related = related; this.prefix = prefix; }
		
		static final Mode EQ = new Mode( "equal to", "" );
		static final Mode MAX = new Mode( "less than", "maxEx-" ); 
		static final Mode MIN = new Mode( "greater than", "minEx-" );
		}

    private String resRequest(String base, Property p, Resource o )
    	{
    	String shortP = shorten( p.getURI() );
    	String oURI = o.getURI();
		String shortO = shorten( oURI );
    	if (shortO == null) shortO = oURI;
    	String uri = withArgs( base, shortP + "=" + shortO );
    	String image = "[similar]";
    	String title = "click to see other items with the same property-value";
    	String link = "<a href='" + oURI + ".html'>&Delta;</a>";
    	return "<a href='" + uri + "' title='" + protect(title) + "'>" + image + "</a>" + " " + link;
    	}
	
	private String withArgs( String base, String args )
		{
		return base + (base.contains("?") ? "&" : "?") + args;
		}

	private String intRequest(String base, Mode m, Property p, String value ) 
    	{
    	String shortP = shorten( p.getURI() );
    	String uri = withArgs( base,  m.prefix + shortP + "=" + value );
    	String image = 
    		m == Mode.MAX ? "&#0171;" 
    		: m == Mode.MIN ? "&#0187;"
    		: value
    		;
    	return "<a href='" + uri + "' title='" + protect( rangeTitle(m, value) ) + "'>" + image + "</a>";
    	}

	private String rangeTitle( Mode m, String value) {
		return "click to show only items with values " + m.related + " " + value;
	}

	private void linkyBits( StringBuilder textBody, Resource anchor )
        {
        Statement first = anchor.getProperty( XHV.first );
        Statement next = anchor.getProperty( XHV.next );
        Statement prev = anchor.getProperty( XHV.prev );        
        textBody
        	.append( "\n" )
        	.append( "<table class='paging-links' width='100%'>" )
        	.append( "<tr>" )
        	.append( paging_link( "first", "left", first ) )
        	.append( paging_link( "prev", "center", prev ) )
        	.append( paging_link( "next", "right", next ) )
        	.append("</tr>" )
        	.append( "</table>" )
        	;      
        }
    
    private String paging_link( String label, String align, Statement s ) 
    	{
    	if (s == null) return "";
    	Resource r = s.getResource();
        return "<td text-align='" + align + "'><a href='" + r.getURI() + "'>" + safe(label) + "</a></td>";
    	}
    
    private String brief( RDFNode x )
        {
        return
            x.isAnon() ? x.asNode().getBlankNodeLabel()
            : x.isResource() ? activeQname( (Resource) x ) 
            : protect( x.asNode().getLiteralLexicalForm() )
            ;
        }

    private String activeQname( Resource x ) 
    	{
    	return "<a href='" + x.getURI() + "'>" + qname(x) + "</a>";
    	}

	private String protect(String s ) 
    	{
    	return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;" );
    	}

	private String qname( Resource x )
        { 
    	String s = shorten( x.getURI() );
    	return s == null ? x.getModel().shortForm( x.getURI() ) : s;
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
