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
import com.hp.hpl.jena.rdf.model.*;

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

        for (DisplayNode item: page.items()) {
            HierarchyNode n = new HierarchyNode( new PropertyPath(), null, item );
            roots.add( n );
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Expand the hierarchy from the given roots
     */
    public void expand( ShortNameRenderer snr ) {
        DisplayHierarchyContext context = initialiseContext( page, snr );

        while (!context.completed()) {
            HierarchyNode node = context.queue().remove();
            expandNode( context, node );
        }
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Expand a hierarchy node by extracting the properties of the node, matching
     * paths to properties, and appending the children to the expansion queue if appropriate.
     *
     * @param context
     * @param node
     */
    protected void expandNode( DisplayHierarchyContext context, HierarchyNode node ) {
        List<Statement> arcs = node.rdfNode().getDisplayProperties();
        Set<PropertyPath> paths = node.isRoot() ? context.basePaths() : node.explicitPaths();

        for (Statement s: arcs) {
            Property p = s.getPredicate();
            PropertyPath pathTo = node.pathTo().with( null, p.getURI(), context.shortNameRenderer() );
            HierarchyNode child = new HierarchyNode( pathTo, node, context.wrap( s.getObject() ) );

            Set<PropertyPath> matchingPaths = matchingPaths( context, s.getPredicate(), paths );
            child.explicitPaths().addAll( matchingPaths );

            node.children().add( child );
            context.see( child.rdfNode() );

            if (!child.isLeaf( context )) {
                context.queue().add( child );
            }
        }
    }

    /**
     * Select which of the given property paths matches the given property p. The match is
     * determined by {@link PropertyPath#beginsWith(Property, ShortNameRenderer)}
     *
     * @param context The current context, including the short name renderer
     * @param p An RDF property
     * @param paths The paths for consideration
     * @return A sub-set of <code>paths</code>, possibly empty, which start with the given property
     */
    protected Set<PropertyPath> matchingPaths( DisplayHierarchyContext context, Property p, Set<PropertyPath> paths ) {
        Set<PropertyPath> matching = new HashSet<PropertyPath>();

        for (PropertyPath path: paths) {
            if (path.beginsWith( p, context.shortNameRenderer() )) {
                matching.add( path );
            }
        }

        return matching;
    }

    /** @return A new context bookkeeping object */
    protected DisplayHierarchyContext initialiseContext( Page page, ShortNameRenderer snr ) {
        DisplayHierarchyContext context = new DisplayHierarchyContext();

        context.setPage( page );
        context.setShortNameRenderer( snr );

        context.queue().addAll( roots );
        for (HierarchyNode n: roots) {
            context.see( n.rdfNode );
        }
        context.setBasePropertyPaths( new HashSet<PropertyPath>( page.currentPropertyPaths() ) );

        return context;
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
        private Set<PropertyPath> explicitPaths = new HashSet<PropertyPath>();

        /** The parent node, null for root nodes */
        private HierarchyNode parent;

        /** The RDF node that is being presented at this level */
        private DisplayNode rdfNode;

        /** The list of child nodes of this node */
        private List<HierarchyNode> children = new ArrayList<DisplayHierarchy.HierarchyNode>();

        /** Constructor */
        public HierarchyNode( PropertyPath pathTo, HierarchyNode parent, DisplayNode rdfNode ) {
            this.pathTo = pathTo;
            this.parent = parent;
            this.rdfNode = rdfNode;
        }

        /** @return The path to this node */
        public PropertyPath pathTo() {
            return pathTo;
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
        public HierarchyNode parent() {
            return parent;
        }

        /** @return True if this node is a root of the hierarchy */
        public boolean isRoot() {
            return parent == null;
        }

        /** @return The RDF node that this hierarchy node is presenting */
        public DisplayNode rdfNode() {
            return rdfNode;
        }

        /** @return True if this resource has already been seen on this branch */
        public boolean isLoop() {
            return (parent() != null) ? parent().findAncestor( rdfNode() ) : false;
        }

        /** @return True if this node's RDF node equals <code>resource</code>, or if any of my ancestors do */
        public boolean findAncestor( DisplayNode resource ) {
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

        /** @return The list of children of this node */
        public List<HierarchyNode> children() {
            return children;
        }
    }

    /**
     * A collection of the state information we keep while expanding the display hierarchy
     */
    public class DisplayHierarchyContext {
        /** A queue of the non-expanded nodes */
        private Queue<HierarchyNode> queue = new ArrayDeque<HierarchyNode>();

        /** A list of the resource URIs we have seen */
        private Set<Resource> seen = new HashSet<Resource>();

        /** The base set of property paths we have to deal with */
        private Set<PropertyPath> basePaths;

        /** The short name rendering service */
        private ShortNameRenderer shortNameRenderer;

        /** The current page */
        private Page page;

        /** @return The current hierarchy node queue */
        public Queue<HierarchyNode> queue() {
            return this.queue;
        }

        /** @return True if the given node is a resource which has already been seen during the expansion */
        public boolean isSeen( RDFNodeWrapper r ) {
            return r.isResource() && this.seen.contains( r );
        }

        /** Add a node to the set of seen resources */
        public void see( RDFNodeWrapper n ) {
            if (n.isResource()) {
                seen.add( n.asResource() );
            }
        }

        /** Add a set of resources to the seen set */
        public void see( Iterable<RDFNodeWrapper> ns ) {
            for (RDFNodeWrapper n: ns) {
                see( n );
            }
        }

        /** Set the base list of known property paths for the current view */
        public void setBasePropertyPaths( Set<PropertyPath> paths ) {
            this.basePaths = paths;
        }

        /** @return The base set of property paths */
        public Set<PropertyPath> basePaths() {
            return basePaths;
        }

        /** @return True if the queue has been exhausted */
        public boolean completed() {
            return queue().isEmpty();
        }

        /** Set the short name renderer */
        public void setShortNameRenderer( ShortNameRenderer snr ) {
            this.shortNameRenderer = snr;
        }

        /** @return The short name renderer */
        public ShortNameRenderer shortNameRenderer() {
            return this.shortNameRenderer;
        }

        /** @return A wrapped version of the given RDF node */
        public DisplayNode wrap( RDFNode n ) {
            return new DisplayNode( this.page, n );
        }

        /** Set the model wrapper */
        public void setPage( Page page ) {
            this.page = page;
        }
    }

}
