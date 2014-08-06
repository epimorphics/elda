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
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.Property;


/**
 *  A node in the unfolded display hierarchy starting at the item roots. In the case
 *  of properties with multiple values, a node may be a group that has siblings.
 */
public class DisplayHierarchyNode
{
    /***********************************/
    /* Constants                       */
    /***********************************/

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

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}
