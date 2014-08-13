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

import org.apache.jena.atlas.lib.StrUtils;

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

    public static final String SORT_PARAM = "_sort";
    public static final String PAGE_PARAM = "_page";

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

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public DisplayHierarchyNode( PropertyPath pathTo, DisplayHierarchyNode parent, DisplayRdfNode rdfNode ) {
        this.pathTo = pathTo;
        this.parent = parent;
        this.rdfNode = rdfNode;

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
        return parent == null;
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
               isLoop() ||
               (!isRoot() &&
                !isOnExplicitPath() &&
                context.isSeen( rdfNode() ));
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

        if (isResource()) {
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
        String param = terminalLink().getName();
        String valueStr = isLiteral ? rdfNode().getLexicalForm() : rdfNode().getName();

        if (isNumeric) {
            links.add( generateLink( "max-" + param, param, valueStr, valueStr, "&le;", "filter-less-than", true, page ));
        }

        if (isLiteral) {
            links.add( generateLink( param, param, valueStr, valueStr, "is", "filter-equals", true, page ));
        }
        else if (!rdfNode().isAnon()){
            links.add( generateLink( param, param, rdfNode.getShortURI(), valueStr, "is", "filter-equals", true, page ));
        }

        if (isNumeric) {
            links.add( generateLink( "min-" + param, param, valueStr, valueStr, "&ge;", "filter-greater-than", true, page ));
        }

        links.add( generateSortLink( param, "sort sort-asc", true, page ));
        links.add( generateSortLink( param, "sort sort-desc", false, page ));

        return links;
    }


    /* Convenience methods which delegate to the same method on the encapsulated RDFNodeWrapper */

    /** @see {@link RDFNodeWrapper#isResource()} */
    public boolean isResource() {
        return rdfNode().isResource();
    }

    /** @see {@link RDFNodeWrapper#isLiteral()} */
    public boolean isLiteral() {
        return rdfNode().isLiteral();
    }

    /** @see {@link RDFNodeWrapper#isAnon()} */
    public boolean isAnon() {
        return rdfNode().isAnon();
    }

    /** @see {@link RDFNodeWrapper#isList()} */
    public boolean isList() {
        return rdfNode().isList();
    }

    /** @see {@link RDFNodeWrapper#getLexicalForm()} */
    public String getLexicalForm() {
        return rdfNode().getLexicalForm();
    }

    /** @see {@link RDFNodeWrapper#getName()} */
    public String getName() {
        return rdfNode().getName();
    }

    /** @see {@link RDFNodeWrapper#getValue()} */
    public Object getValue() {
        return rdfNode().getValue();
    }

    /** @see {@link RDFNodeWrapper#getURI()} */
    public String getURI() {
        return rdfNode().getURI();
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
        EldaURL pageURL = page.pageURL();
        String prompt = "require: ";

        if (pageURL.hasParameter( paramName, paramValue )) {
            op = OPERATION.REMOVE;
            linkIcon = "fa-minus-circle";
            prompt = "remove constraint: ";
        }

        return new Link( String.format( "<i class='fa %s'></i> %s%s %s %s", linkIcon, prompt, paramNameLabel, rel, paramValueLabel ),
                         pageURL.withParameter( op, paramName, paramValue ),
                         hint );
    }

    /**
     * Generate a link to an adjacent point in API space by adding or removing a sort parameter.
     * If the URL already includes a sort on "foo", then adding a sort on "-foo" will remove
     * the "foo" sort, and vice-versa.
     *
     * @param paramName The name of the parameter to change
     * @param hint CSS hint
     * @param asc If true, sort ascending
     * @param page The current page object
     * @return The new link
     */
    protected Link generateSortLink( String paramName, String hint, boolean asc, Page page ) {
        OPERATION op = OPERATION.ADD;
        String linkIcon = "fa-chevron-circle-" + (asc ? "down" : "up");
        EldaURL pageURL = page.pageURL();
        String prompt = "sort by: ";
        String sortOn =         (asc ? "" : "-") + paramName;
        String converseSortOn = (asc ? "-" : "") + paramName;

        if (pageURL.hasParameter( SORT_PARAM, sortOn )) {
            op = OPERATION.REMOVE;
            linkIcon = "fa-minus-circle";
            prompt = "remove sorting on: ";
        }

        return new Link( String.format( "<i class='fa %s'></i> %s%s%s", linkIcon, prompt, paramName, asc ? "" : " (descending)"),
                         pageURL.withParameter( OPERATION.REMOVE, SORT_PARAM, converseSortOn )
                                .withParameter( OPERATION.SET, PAGE_PARAM, "0" )
                                .withParameter( op, SORT_PARAM, sortOn ),
                         hint );
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}
