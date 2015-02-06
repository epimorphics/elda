/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jena.atlas.lib.StrUtils;

import com.epimorphics.lda.query.QueryParameter;
import com.epimorphics.lda.renderers.common.DisplayHierarchy.DisplayHierarchyContext;
import com.epimorphics.lda.renderers.common.EldaURL.OPERATION;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.epimorphics.rdfutil.RDFUtil;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;


/**
 *  A node in the unfolded display hierarchy starting at the item roots. In the case
 *  of properties with multiple values, a node may be a group that has siblings.
 */
public class DisplayHierarchyNode
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    // commented out sorting by labels, see discussion on issue #90
//    public static final String SORT_BY_LABELS_FORMAT = "%1$s%2$s.label,%1$s%2$s.prefLabel,%1$s%2$s";
    public static final String SORT_BY_FORMAT = "%1$s%2$s";

    /** Maximum label length in a related link */
    public static final int MAX_RELATED_LINK_LABEL_LENGTH = 30;

    /***********************************/
    /* Static variables                */
    /***********************************/

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The path leading to this node */
    private PropertyPath pathTo;

    /** For nodes that are part of an explicit property path */
    private Set<PropertyPath> explicitPaths = new HashSet<PropertyPath>();

    /** The parent node, null for root nodes */
    private DisplayHierarchyNode parent;

    /** The RDF node that is being presented at this level */
    DisplayRdfNode rdfNode;

    /** The list of child nodes of this node */
    private List<DisplayHierarchyNode> children = new ArrayList<DisplayHierarchyNode>();

    /** The list of sibling nodes of this node (nodes that share the same property path) */
    private List<DisplayHierarchyNode> siblings = new ArrayList<DisplayHierarchyNode>();

    /** List of presentation hints */
    private List<String> hints = new ArrayList<String>();

    /** Short name renderer */
    private ShortNameRenderer shortNameRenderer;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public DisplayHierarchyNode( PropertyPath pathTo, DisplayHierarchyNode parent,
                                 DisplayRdfNode rdfNode, ShortNameRenderer shortNameRenderer ) {
        this.pathTo = pathTo;
        this.parent = parent;
        this.rdfNode = rdfNode;
        this.shortNameRenderer = shortNameRenderer;

        if (parent != null) {
            parent.children().add( this );
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /** @return The path to this node */
    public PropertyPath pathTo() {
        return pathTo;
    }

    /** @return The RDF predicate linking from the parent node to this one */
    public RDFNodeWrapper terminalLink() {
        Property link = pathTo().terminal();
        return (link == null) ? null : new RDFNodeWrapper( rdfNode().getModelW(), link );
    }

    /** @return True if this node is explicitly on a property path */
    public boolean isOnExplicitPath() {
        return !explicitPaths.isEmpty();
    }

    /** @return The explicit property path that this expansion is following */
    public Set<PropertyPath> explicitPaths() {
        return explicitPaths;
    }

    /** @return The parent node */
    public DisplayHierarchyNode parent() {
        return parent;
    }

    /** @return True if this node is a root of the hierarchy */
    public boolean isRoot() {
        return pathTo().isEmpty();
    }

    /** @return The RDF node that this hierarchy node is presenting */
    public DisplayRdfNode rdfNode() {
        return rdfNode;
    }

    /** @return True if this resource has already been seen on this branch */
    public boolean isLoop() {
        return (parent() != null) ? parent().findAncestor( rdfNode() ) : false;
    }

    /** @return True if this node's RDF node equals <code>resource</code>, or if any of my ancestors do */
    public boolean findAncestor( DisplayRdfNode resource ) {
        if (rdfNode().equals( resource )) {
            return true;
        }
        else if (parent() != null) {
            return parent().findAncestor( resource );
        }
        else {
            return false;
        }
    }

    /**
     * A node is a leaf of the hierarchy if any of the following apply:
     * <ul><li>it is a literal</li>
     *     <li>it has already occurred among its own ancestors (ie is a loop)</li>
     *     <li>it is not a top-level root node, has been previously expanded
     *         and is not on an explicit property path</li>
     * </ul>
     * @param context The current context, which contains the list of seen nodes
     * @return True if this is a leaf node of this hierarchy
     */
    public boolean isLeaf( DisplayHierarchyContext context ) {
        return rdfNode().isLiteral() ||
               !(isRoot() ||
                 rdfNode().isAnon() ||
                 isOnExplicitPath() ||
                 !context.isSeen( rdfNode() ));
    }

    /** @return The list of children of this node. */
    public List<DisplayHierarchyNode> children() {
        return children;
    }

    /** Add a sibling node to this node */
    public void addSibling( DisplayHierarchyNode sibling ) {
        siblings.add( sibling );
    }

    /** @return A list of the siblings of this node, which may be empty but is not null */
    public List<DisplayHierarchyNode> siblings() {
        return siblings;
    }

    /** @return True if this node has at least one sibling */
    public boolean hasSiblings() {
        return !siblings.isEmpty();
    }

    /** Add a new hint to this node's list of display hints */
    public void addHint( String hint ) {
        hints.add( hint );
    }

    /** @return The list of hints joined into a string */
    public String hintsString() {
        return StrUtils.strjoin( " ", hints );
    }

    /** @return True if the given string is one of this node's hints */
    public boolean hasHint( String hint ) {
        return hints.contains( hint );
    }

    /**
     * Return true if this node is a <em>simple</em> resource node: that is,
     * it is a resource, and has no properties other than either
     * <code>rdfs:label</code> or <code>skos:prefLabel</code>.
     * @return True for nodes which can be displayed very simply
     */
    public boolean isSimpleResource() {
        boolean isSimple = false;

        if (isResource() && !isAnon()) {
            List<Statement> s = rdfNode().asResource().listProperties().toList();

            if (s.size() == 0) {
                isSimple = true;
            }
            else if (s.size() == 1) {
                Property p = s.get( 0 ).getPredicate();
                for (Property labelP: RDFUtil.labelProps) {
                    isSimple = isSimple || p.equals( labelP );
                }
            }
        }

        return isSimple;
    }

    /** @return An HTML marked-up string of related links */
    public String relatedLinksHTML() {
        List<String> html = new ArrayList<String>();

        html.add( "<ul class=''>" );
        for (Link l: relatedLinks()) {
            html.add( l.toHTMLString( "li" ) );
        }
        html.add( "</ul>" );

        return StrUtils.strjoin( "\n", html );
    }

    /** @return A list of the related links to this node */
    public List<Link> relatedLinks() {
        List<Link> links = new ArrayList<Link>();
        boolean isLiteral = rdfNode().isLiteral();
        boolean isNumeric = isLiteral && (rdfNode().getValue() instanceof Number);
        Page page = rdfNode().page();
        String param = pathTo().toString();
        String paramHTML = pathTo.toHTMLString();
        String valueStr = isLiteral ? rdfNode().getLexicalForm() : rdfNode.getName();
        String valueLabel = "<code class='rdf-value'>" + StringEscapeUtils.escapeHtml( truncateToMaxLength( valueStr ) ) + "</code>";

        if (isNumeric) {
            links.add( generateLink( "max-" + param, paramHTML, valueStr, valueLabel, "&le;", "filter-less-than", true, page ));
        }

        if (isLiteral) {
            links.add( generateLink( param, paramHTML, valueStr, valueLabel, "to be", "filter-equals", true, page ));
        }
        else if (!rdfNode().isAnon()){
            String shortName = null;
            if (shortNameRenderer != null) {
                shortName = shortNameRenderer.lookupURI( rdfNode().getURI() );
            }
            String uriValue = (shortName == null) ? rdfNode().getURI() : shortName;

            links.add( generateLink( param, paramHTML, uriValue, valueLabel, "to be", "filter-equals", true, page ));
        }

        if (isNumeric) {
            links.add( generateLink( "min-" + param, paramHTML, valueStr, valueLabel, "&ge;", "filter-greater-than", true, page ));
        }

        links.add( generateSortLink( param, paramHTML, "sort sort-asc", true, page ));
        links.add( generateSortLink( param, paramHTML, "sort sort-desc", false, page ));

        return links;
    }

    /** @return True if this node has all of the given properties */
    public boolean hasAllProperties( Object... properties ) {
        return rdfNode().hasAllProperties( properties );
    }

    /** @return True if this node has all of the given properties */
    public boolean hasAllProperties( List<Object> properties ) {
        return rdfNode().hasAllProperties( properties.toArray() );
    }

    /**
     * Promote the given properties to the front of the list of properties attached
     * to this node. For example, if the children of this node are
     * <code>a,b,c,d,e</code>, and we <code>pullToStart(c,d,f)</code>
     * the children of the node will become <code>c,d,a,b,e</code>
     *
     * @param properties RDF properties which will identify the children of this node to
     * move to the front of the children list. Order within the <code>properties</code>
     * is preserved.
     */
    public void pullToStart( Object... properties ) {
        for( int j = properties.length - 1; j >= 0; j-- ) {
            Property prop = rdfNode().toProperty( properties[j] );
            int index = indexOfChildProperty( prop );

            if (index > 0) {
                DisplayHierarchyNode n = children.get( index );
                for (int i = index; i > 0; i--) {
                    children.set( i, children.get( i - 1 ) );
                }
                children.set( 0, n );
            }
        }
    }

    /**
     * Promote the given properties to the front of the list of properties attached
     * to this node. For example, if the children of this node are
     * <code>a,b,c,d,e</code>, and we <code>pullToStart(c,d,f)</code>
     * the children of the node will become <code>c,d,a,b,e</code>
     *
     * @param properties RDF properties which will identify the children of this node to
     * move to the front of the children list. Order within the <code>properties</code>
     * is preserved.
     */
    public void pullToStart( List<Object> properties ) {
        pullToStart( properties.toArray() );
    }

    /**
     * Extract the given properties, so that they can be treated in a particular
     * way by the renderer.
     * @param properties One or more RDF properties as Property objects or qName strings
     * @return A non-null but possibly empty list of the child nodes of this node that are
     * connected to this node by one of the given properties.
     */
    public List<DisplayHierarchyNode> extractByPredicate( Object... properties ) {
        List<DisplayHierarchyNode> extracted = new ArrayList<DisplayHierarchyNode>();

        for (Object p: properties) {
            Property prop = rdfNode().toProperty( p );
            int index = indexOfChildProperty( prop );

            if (index >= 0) {
                extracted.add( children.remove( index ) );
            }
        }

        return extracted;
    }

    /**
     * Extract the given properties, so that they can be treated in a particular
     * way by the renderer.
     * @param properties One or more RDF properties as Property objects or qName strings
     * @return A non-null but possibly empty list of the child nodes of this node that are
     * connected to this node by one of the given properties.
     */
    public List<DisplayHierarchyNode> extractByPredicate( List<Object> properties ) {
        return extractByPredicate( properties.toArray() );
    }

    /* Convenience methods which delegate to the same method on the encapsulated RDFNodeWrapper */

    /** @see RDFNodeWrapper#isResource() */
    public boolean isResource() {
        return rdfNode().isResource();
    }

    /** @see RDFNodeWrapper#isLiteral() */
    public boolean isLiteral() {
        return rdfNode().isLiteral();
    }

    /** @see RDFNodeWrapper#isAnon() */
    public boolean isAnon() {
        return rdfNode().isAnon();
    }

    /** @see RDFNodeWrapper#isList() */
    public boolean isList() {
        return rdfNode().isList();
    }

    /** @see RDFNodeWrapper#getLexicalForm() */
    public String getLexicalForm() {
        return rdfNode().getLexicalForm();
    }

    /** @see RDFNodeWrapper#getName() */
    public String getName() {
        String name = rdfNode().getName();
        return name.equals( "[]" ) ? "" : name;
    }

    /** @see RDFNodeWrapper#getValue() */
    public Object getValue() {
        return rdfNode().getValue();
    }

    /** @see RDFNodeWrapper#getURI() */
    public String getURI() {
        return rdfNode().getURI();
    }
    
    /** @return A string presentation of this node for debugging */
    @Override
    public String toString() {
        String ep = "[";
        String s = "";
        
        for (PropertyPath p: explicitPaths) {
            ep = ep + p.toString() + s;
            s = ",";
        }
        ep = ep + "]";
        
        return "DisplayHierarchyNode{ node: " + rdfNode + 
                ", parent:" + (parent == null ? "null" : parent.rdfNode()) +
                ", pathTo: " + pathTo +
                ", nSiblings: " + siblings.size() +
                ", explicitPaths: " + ep +
                "}";
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Generate a link to an adjacent point in API space, by changing the value of a parameter
     * of the URI. By default, this is an addition to the URI, but if the parameter is already set,
     * then it becomes a remove operation.
     *
     * @param paramName The name of the parameter to change
     * @param paramNameLabel Presentation form of paramName
     * @param paramValue The value to change
     * @param paramValueLabel The value on the label to show to the user
     * @param rel The relationship of the parameter value to the new state
     * @param hint CSS hint
     * @param set If true, set the value rather than add it (see {@link OPERATION})
     * @param page The current page object
     * @return The new link
     */
    protected Link generateLink( String paramName, String paramNameLabel,
                                 String paramValue, String paramValueLabel,
                                 String rel, String hint, boolean set, Page page ) {
        OPERATION op = set ? OPERATION.SET : OPERATION.ADD;
        String linkIcon = "fa-plus-circle";
        EldaURL pageURL = page.isItemEndpoint() ? page.pageURL().parentURL() : page.pageURL();
        String prompt = "require ";
        String closeQuote = "";

        if (pageURL.hasParameter( paramName, paramValue )) {
            op = OPERATION.REMOVE;
            linkIcon = "fa-minus-circle";
            prompt = "remove <em>'require ";
            closeQuote = "'</em>";
        }

        return new Link( String.format( "<i class='fa %s'></i> %s%s %s %s%s",
                                        linkIcon, prompt,
                                        paramNameLabel, rel,
                                        paramValueLabel, closeQuote ),
                         pageURL.withParameter( op, paramName, paramValue ),
                         hint );
    }

    /**
     * Generate a link to an adjacent point in API space by adding or removing a sort parameter.
     * If the URL already includes a sort on "foo", then adding a sort on "-foo" will remove
     * the "foo" sort, and vice-versa.
     *
     * @param paramName The name of the parameter to change
     * @param paramHTML The parameter's name in HTML form
     * @param hint CSS hint
     * @param asc If true, sort ascending
     * @param page The current page object
     * @return The new link
     */
    protected Link generateSortLink( String paramName, String paramHTML, String hint, boolean asc, Page page ) {
        OPERATION op = OPERATION.ADD;
        String linkIcon = "fa-chevron-circle-" + (asc ? "up" : "down");
        EldaURL pageURL = page.pageURL();
        String prompt = "sort by ";

        String sDir = asc ? "" : "-";
        String sortOn = String.format( SORT_BY_FORMAT, sDir, paramName );

        sDir = asc ? "-" : "";
        String converseSortOn = String.format( SORT_BY_FORMAT, sDir, paramName );

        if (pageURL.hasParameter( QueryParameter._SORT, sortOn )) {
            op = OPERATION.REMOVE;
            linkIcon = "fa-minus-circle";
            prompt = "remove sorting on: ";
        }

        return new Link( String.format( "<i class='fa %s'></i> %s%s%s", linkIcon, prompt, paramHTML, asc ? "" : " (descending)"),
                         pageURL.withParameter( OPERATION.REMOVE, QueryParameter._SORT, converseSortOn )
                                .withParameter( OPERATION.SET, QueryParameter._PAGE, "0" )
                                .withParameter( op, QueryParameter._SORT, sortOn ),
                         hint );
    }

    /** @return The index of the child node with the given property, or -1 if not found */
    protected int indexOfChildProperty( Property prop ) {
        int index = -1;

        for (int i = 0; index < 0 && i < children().size(); i++) {
            DisplayHierarchyNode child = children().get( i );
            if (child.terminalLink().asResource().equals(  prop )) {
                index = i;
            }
        }

        return index;
    }

    private String ellipsis = "...";
    private int maxLength = MAX_RELATED_LINK_LABEL_LENGTH - ellipsis.length();

    /**
     * Ensure that a string does not exceed the given length
     */
    private String truncateToMaxLength( String s ) {
        if (s.length() <= maxLength) {
            return s;
        }
        else {
            return s.substring( 0, maxLength ) + ellipsis;
        }
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}
