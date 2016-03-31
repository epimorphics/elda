/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;

import java.io.StringWriter;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.renderers.common.EldaURL.*;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.vocabularies.*;
import com.epimorphics.rdfutil.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;

/**
 * Value object representing the page of results returned by Elda's query
 * processing. Corresponds to a single resource of type <code>api:Page</code>.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class Page extends CommonNodeWrapper
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** Indicate no available numeric value */
    public static final int NO_VALUE = -1;

    /** By default, we assume we're seeing the basic view */
    public static final String DEFAULT_VIEW_NAME = "basic";

    /***********************************/
    /* Static variables                */
    /***********************************/

    private static final Logger log = LoggerFactory.getLogger( Page.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** Cached var bindings */
    private Map<String, String> varBindingsMap;

    /** Reference to short-name rendering service */
    private ShortNameRenderer shortNameRenderer;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new page object corresponding to the <code>root</code> object
     * in model <code>mw</code>.
     *
     * @param mw
     *            A wrapper around the model containing the results from the API
     * @param root
     *            The root resource of this page
     */
    public Page( ResultsModel mw, Resource root ) {
        super( mw, root );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Initialise the short-name renderer that this page has access to, basing
     * it on an existing short name service
     *
     * @param sns
     */
    public void initialiseShortNameRenderer( ShortnameService sns ) {
        if (shortNameRenderer == null) {
            shortNameRenderer = new ShortNameRenderer( sns, termBindings() );
        }
    }

    /** @return The short name renderer for this page */
    public ShortNameRenderer shortNameRenderer() {
        return shortNameRenderer;
    }

    /** @return The page URL in a form that supports generating related URLs */
    public EldaURL pageURL() {
        return new EldaURL( getURI() );
    }

    /**
     * @return True if the page denotes a single item, or false for a list
     *         endpoint
     */
    public boolean isItemEndpoint() {
        return asResource().hasProperty( FOAF.primaryTopic );
    }

    /**
     * @return The current page number, starting from zero. Return -1 if this
     *         page does not have a specified page number
     */
    public int pageNumber() {
        return getInt( API.page, NO_VALUE );
    }

    /**
     * @return The number of items per page. Return -1 if this page does not
     *         specify the number of items per page
     */
    public int itemsPerPage() {
        return getInt( OpenSearch.itemsPerPage, NO_VALUE );
    }
    
    /**
     * @return The total number of results in the resultset, or -1 if not specified.
     */
    public int totalResults() {
        return getInt( OpenSearch.totalResults, NO_VALUE );
    }
    
    /** 
     * @return A string summarising the count of results, or null
     */
    public String resultsCountSummary() {
        String s = null;
        int total = totalResults();
        
        if (total >= 0) {
            int perPage = itemsPerPage();
            int from = startIndex();
            
            if (perPage > 0 && from >= 0 && total > 0) {
                int to = from + perPage - 1;
                if (to > total) {
                    to = total;
                }
                
                s = String.format( "Showing items %d to %d of %d", from, to, total );
            }
            else {
                String plural = (total == 1) ? "" : "s";
                s = String.format( "%d result%s", total, plural );
            }
        }
        
        return s;
    }

    /**
     * @return The starting index for this page, starting from one. Return -1 if
     *         this page does not specify the starting index.
     */
    public int startIndex() {
        return getInt( OpenSearch.startIndex, NO_VALUE );
    }

    /**
     * @return The list that this page is part of
     */
    public Resource isPartOf() {
        return getResource( DCTerms.isPartOf );
    }

    /**
     * @return A resource denoting the API endpoint specification.
     */
    public Resource definition() {
        return getResource( API.definition );
    }

    /**
     * @return The URL for the extended metadata for this page
     */
    public String extendedMetadataURL() {
        com.epimorphics.rdfutil.RDFNodeWrapper nw = getPropertyValue( API.extendedMetadataVersion );
        return (nw == null) ? null : nw.getLexicalForm();
    }

    /**
     * @return The resource denoting the first page of results, or null
     */
    public Resource firstPage() {
        return getResource( XHV.first );
    }

    /**
     * @return The resource denoting the previous page of results, or null
     */
    public Resource prevPage() {
        return getResource( XHV.prev );
    }

    /**
     * @return The resource denoting the next page of results, or null
     */
    public Resource nextPage() {
        return getResource( XHV.next );
    }

    /**
     * @return The resource denoting the last page of results, or null
     */
    public Resource lastPage() {
        return getResource( ResourceFactory.createProperty( XHV.ns + "last" ) );
    }

    /**
     * @return True if this page of results has at least one element of related
     *         page data
     */
    public boolean hasPageData() {
        return pageNumber() != NO_VALUE || itemsPerPage() != NO_VALUE || firstPage() != null || lastPage() != null
                || nextPage() != null || prevPage() != null;
    }

    /**
     * @return A list of the formats that this page is available in
     */
    public List<PageFormat> formats() {
        List<PageFormat> pfs = new ArrayList<PageFormat>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n : pageMetadataRoot().listPropertyValues( DCTerms.hasFormat )) {
            pfs.add( new PageFormat( this, n.asResource() ) );
        }

        Collections.sort( pfs );

        return pfs;
    }

    /**
     * @return A list of the views that this page defines
     */
    public List<EldaView> views() {
        List<EldaView> vs = new ArrayList<EldaView>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n : pageMetadataRoot().listPropertyValues( DCTerms.hasVersion )) {
            vs.add( new EldaView( this, n.asResource() ) );
        }

        Collections.sort( vs );

        return vs;
    }

    /**
     * Return a list of the term bindings, which associate a short name with a
     * resource.
     *
     * @return A list of the term bindings defined on this page
     */
    public List<Binding<Resource>> termBindings() {
        List<Binding<Resource>> bindings = new ArrayList<Binding<Resource>>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n : connectedNodes( "api:wasResultOf/api:termBinding" )) {
            String label = n.getPropertyValue( API.label ).getLexicalForm();
            Resource res = n.getPropertyValue( API.property ).asResource();

            bindings.add( new Binding<Resource>( label, res ) );
        }

        return bindings;
    }

    /**
     * Return a list of the variable bindings, which associate a variable name
     * with a textual value
     *
     * @return A list of the variable bindings
     */
    public List<Binding<String>> varBindings() {
        List<Binding<String>> bindings = new ArrayList<Binding<String>>();

        for (com.epimorphics.rdfutil.RDFNodeWrapper n : connectedNodes( "api:wasResultOf/api:variableBinding" )) {
            String label = n.getPropertyValue( API.label ).getLexicalForm();
            String value = n.getPropertyValue( API.value ).getLexicalForm();

            bindings.add( new Binding<String>( label, value ) );
        }

        return bindings;
    }

    /**
     * @return The {@link #varBindings()} as a map. Cached for performance.
     */
    public Map<String, String> varBindingsMap() {
        if (this.varBindingsMap == null) {
            this.varBindingsMap = new HashMap<String, String>();

            for (Binding<String> binding : varBindings()) {
                this.varBindingsMap.put( binding.label(), binding.value() );
            }
        }

        return this.varBindingsMap;
    }

    /**
     * @return A list of the items on this page, as a list of
     *         {@link DisplayRdfNode}
     */
    public List<DisplayRdfNode> items() {
        List<DisplayRdfNode> items = new ArrayList<DisplayRdfNode>();
        
        if (isItemEndpoint()) {
            Resource item = getPropertyValue( FOAF.primaryTopic ).asResource();
            items.add( new DisplayRdfNode( this, item ) );
        }
        else {
            RDFList itemList = getPropertyValue( API.items ).asResource().as( RDFList.class );
            for (RDFNode n : itemList.asJavaList()) {
                Resource item = n.asResource();
                items.add( new DisplayRdfNode( this, item ) );
            }
        }

        return items;
    }

    /** @return The object graph for this page as an RDF model */
    public Model pageObjectModel() {
        return getModelW().getDataset().getNamedModel( ResultsModel.RESULTS_OBJECT_GRAPH );
    }
    
    /**
     * Return the current view, if we can determine what it is. If we can't tell
     * from the given metadata, return the basic view.
     *
     * @return The currently selected view
     */
    public EldaView currentView() {
        String viewName = varBindingsMap().get( ELDA_API.viewName.getLocalName() );
        if (viewName == null) {
            viewName = DEFAULT_VIEW_NAME;
        }

        return findViewByName( viewName );
    }

    /**
     * Return the view that has the given name
     *
     * @param viewName
     *            The name of the view to look for
     * @return An {@link EldaView} object that has the name
     *         <code>viewName</code>
     * @exception EldaException
     *                if there is no such view
     */
    public EldaView findViewByName( String viewName ) {
        EldaView view = null;
        ResIterator i = getModelW().getModel().listSubjectsWithProperty( ELDA_API.viewName, viewName );

        if (!i.hasNext()) {
            if (viewName.equals( EldaView.BasicView.NAME )) {
                view = new EldaView.BasicView( this );
            }
            else if (viewName.equals( EldaView.DescriptionView.NAME )) {
                view = new EldaView.DescriptionView( this );
            }
            else {
                throw new EldaException( "Could not locate view with viewName = " + viewName );
            }
        }
        else {
            Resource viewRoot = i.next();
            if (i.hasNext()) {
                log.warn(ELog.message("ambiguous view name: there is more than one resource with viewName '%s'", viewName));
            }

            view = new EldaView( this, viewRoot );
        }

        return view;
    }

    /**
     * Return a facade object providing a convenience API onto the page metadata
     */
    public PageMetadata metadata() {
        return new PageMetadata( this );
    }

    /**
     * Return a list of all of the property paths that are explicitly defined
     * for this page. These will either be from the <code>api:properties</code>
     * on the view specification, or the <code>_properties</code> variable, or
     * both.
     *
     * @return A list of the property paths that are explicitly included in the
     *         current page.
     */
    public List<PropertyPath> currentPropertyPaths() {
        List<PropertyPath> paths = new ArrayList<PropertyPath>();

        paths.addAll( currentView().propertyPaths() );
        paths.addAll( queryParamPropertyPaths() );

        return paths;
    }

    /**
     * Synthesise an informative title for this page
     *
     * @return A displayable page title
     */
    public String pageTitle() {
        EldaURL url = new EldaURL( getURI() );
        URLParameterValue _page = url.getParameter( QueryParameter._PAGE );
        String pageNo = (_page == null) ? "0" : _page.toString();
        if (pageNo.equals( "" )) {
            pageNo = "0";
        }

        String rootPath = url.getUri().getPath();

        if (isItemEndpoint()) {
            return "LDA resource at " + rootPath;
        }
        else {
            return String.format( "Elda &ndash; %s &ndash; page %s", rootPath, pageNo );
        }
    }

    /**
     * @return True if at least one item on this page passes the
     *         <code>hasAllProperties</code> test with the given properties
     */
    public boolean itemHasAllProperties( List<Object> properties ) {
        return itemHasAllProperties( properties.toArray() );
    }

    /**
     * @return True if at least one item on this page passes the
     *         <code>hasAllProperties</code> test with the given properties
     */
    public boolean itemHasAllProperties( Object... properties ) {
        boolean atLeastOneItem = false;

        for (DisplayRdfNode item : items()) {
            atLeastOneItem = atLeastOneItem || item.hasAllProperties( properties );
        }

        return atLeastOneItem;
    }

    /**
     * @return A list of all resources in the current results model which each
     *         have all of the given properties
     */
    public List<DisplayRdfNode> resourcesWithAllProperties( List<Object> properties ) {
        List<DisplayRdfNode> rs = new ArrayList<DisplayRdfNode>();

        if (!properties.isEmpty()) {
            Property p = toProperty( properties.get( 0 ) );

            List<Resource> candidates = getModelW().getModel().listSubjectsWithProperty( p ).toList();

            // test the remaining properties
            for (Object p1 : properties) {
                List<Resource> checked = new ArrayList<Resource>();
                Property pp1 = toProperty( p1 );

                for (Resource cand : candidates) {
                    if (cand.hasProperty( pp1 )) {
                        checked.add( cand );
                    }
                }

                candidates = checked;
            }

            for (Resource cand : candidates) {
                rs.add( new DisplayRdfNode( this, cand ) );
            }
        }

        return rs;
    }

    /**
     * @return A list of links to pages that remove one of the currently active
     *         filters
     */
    public List<Link> filterRemovalLinks() {
        List<Link> links = new ArrayList<Link>();
        EldaURL eu = pageURL();

        for (NamedParameterValue p : eu.getParameters( true )) {
            String filterPath = p.name();
            String filterValue = p.toString();
            RDFNodeWrapper n = new CommonNodeWrapper( getModelW(), RDFUtil.asRDFNode( filterValue ) );

            if (shortNameRenderer().isKnownShortnamePath( filterPath )) {
                links.add( createRemovalLink( eu, p.name(), p.name(), filterValue, n.getName() ) );
            }
        }

        return links;
    }

    /**
     * @return A list of links to pages that remove one of the currently active
     *         sorts
     */
    public List<Link> sortRemovalLinks() {
        List<Link> links = new ArrayList<Link>();
        EldaURL eu = pageURL();

        URLParameterValue sorts = eu.getParameter( QueryParameter._SORT );

        if (sorts != null) {
            for (String sortOn : sorts.values()) {
                boolean asc = true;
                String path = sortOn;
                if (sortOn.startsWith( "-" )) {
                    asc = false;
                    path = sortOn.substring( 1 );
                }

                StringWriter buf = new StringWriter();
                String sep = "";

                for (String p: StringUtils.split( path, "." )) {
                    String uri = shortNameRenderer().expand( p );
                    RDFNodeWrapper n = new CommonNodeWrapper( getModelW(), RDFUtil.asRDFNode( uri ) );
                    buf.append( sep );
                    buf.append( n.getName() );
                    sep = " &raquo; ";
                }

                buf.append( asc ? "" : " (descending)" );

                links.add( createRemovalLink( eu, QueryParameter._SORT, "sort by", sortOn,
                                              buf.toString() ) );
            }

        }

        return links;
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * @return A list of the property paths that were defined via the query
     *         parameter _properties
     */
    protected List<PropertyPath> queryParamPropertyPaths() {
        List<PropertyPath> paths = new ArrayList<PropertyPath>();

        if (varBindingsMap.containsKey( QueryParameter._PROPERTIES )) {
            String props = varBindingsMap.get( QueryParameter._PROPERTIES );
            for (String path : props.split( "," )) {
                if (path.length() > 0) {
                    paths.add( new PropertyPath( path ) );
                }
            }
        }

        return paths;
    }

    /** @return A link for removing a filter or sort selection */
    protected Link createRemovalLink( EldaURL pageURL, String paramName, String paramHTML, String paramValue,
            String paramValueHTML ) {
        OPERATION op = OPERATION.REMOVE;
        String linkIcon = "fa-minus-circle";

        return new Link( String.format( "<i class='fa %s'></i> %s %s", linkIcon, paramHTML, paramValueHTML ),
                pageURL.withParameter( op, paramName, paramValue ), "remove-link" );
    }

    /** @return The model wrapper this resource is bound to, cast to {@link ResultsModel} */
    protected ResultsModel resultsModel() {
        return (ResultsModel) getModelW();
    }
    
    /** @return The root resource for the page, in the metadata model only */
    protected RDFNodeWrapper pageMetadataRoot() {
        return resultsModel().metadataRoot();
    }
    
    
    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}
