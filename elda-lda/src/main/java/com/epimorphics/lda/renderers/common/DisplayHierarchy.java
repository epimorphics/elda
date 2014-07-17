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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Represents the resource hierarchy as it is unfolded from the results
 * set graph to form a tree that can be displayed.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DisplayHierarchy
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( DisplayHierarchy.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The page we are creating the hierarchy for */
    private Page page;

    /** The roots of the hierarchy */
    private List<HierarchyNode> roots = new ArrayList<HierarchyNode>();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new display hierarchy for the given page
     * @param page
     */
    public DisplayHierarchy( Page page ) {
        this.page = page;

        for (DisplayResource item: page.items()) {
            HierarchyNode n = new HierarchyNode( new PropertyPath(), new PropertyPath(), null, item );
            roots.add( n );
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Expand the hierarchy from the given roots
     */
    public void expand() {
        Queue<HierarchyNode> queue = new ArrayDeque<HierarchyNode>();
        queue.addAll( roots );

        List<PropertyPath> paths = page.currentPropertyPaths();

        while (!queue.isEmpty()) {
            HierarchyNode node = queue.remove();
            expandNode( queue, paths, node );
        }
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    protected void expandNode( Queue<HierarchyNode> queue, List<PropertyPath> paths, HierarchyNode node ) {
        List<Statement> arcs = node.rdfNode().getDisplayProperties();

        if (node.isRoot()) {

        }
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /**
     * Denotes an expanded node in the hierarchy that unfolds the resultset graph
     */
    public class HierarchyNode {
        /** The path leading to this node */
        private PropertyPath pathTo;

        /** For nodes that are part of an explicit property path */
        private PropertyPath explicitPath;

        /** The parent node, null for root nodes */
        private HierarchyNode parent;

        /** The RDF node that is being presented at this level */
        private DisplayResource rdfNode;

        /** True if this node is a leaf of the hierarchy */
        private boolean isLeaf;

        /** Constructor */
        public HierarchyNode( PropertyPath pathTo, PropertyPath explicitPath, HierarchyNode parent, DisplayResource rdfNode ) {
            this.pathTo = pathTo;
            this.explicitPath = explicitPath;
            this.parent = parent;
            this.rdfNode = rdfNode;
            this.isLeaf = rdfNode.isLiteral() || (explicitPath == null && seen( rdfNode ));
        }

        /** @return The path to this node */
        public PropertyPath pathTo() {
            return pathTo;
        }

        /** @return True if this node is explicitly on a property path */
        public boolean isOnExplicitPath() {
            return explicitPath != null;
        }

        /** @return The explicit property path that this expansion is following */
        public PropertyPath explicitPath() {
            return explicitPath;
        }

        /** @return The parent node */
        public HierarchyNode parent() {
            return parent;
        }

        /** @return True if this node is a root of the hierarchy */
        public boolean isRoot() {
            return parent == null;
        }

        /** @return The RDF node that this hierarchy node is presenting */
        public DisplayResource rdfNode() {
            return rdfNode;
        }

        /** @return True if the given resource has already been seen on this branch */
        public boolean seen( RDFNodeWrapper resource ) {
            if (rdfNode().equals( resource )) {
                return true;
            }
            else if (parent() != null) {
                return parent().seen( resource );
            }
            else {
                return false;
            }
        }

        /** @return True if this node is a leaf, which will be true for literals, and
         * resources that we have already expanded, unless we are following an explicit
         * path.
         */
        public boolean isLeaf() {
            return isLeaf;
        }
    }

}

