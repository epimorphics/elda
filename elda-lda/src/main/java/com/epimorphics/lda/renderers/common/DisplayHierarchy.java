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

import com.epimorphics.rdfutil.PropertyValue;
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

    /** The current short name renderer */
    private ShortNameRenderer shortNameRenderer;

    /** The roots of the hierarchy */
    private List<DisplayHierarchyNode> roots = new ArrayList<DisplayHierarchyNode>();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new display hierarchy for the given page
     * @param page
     */
    public DisplayHierarchy( Page page,  ShortNameRenderer snr ) {
        this.page = page;
        this.shortNameRenderer = snr;

        for (DisplayRdfNode item: page.items()) {
            DisplayHierarchyNode n = new DisplayHierarchyNode( new PropertyPath(), null, item );
            roots.add( n );
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /** @return The root nodes (ie. the top-level item or items) */
    public List<DisplayHierarchyNode> roots() {
        return roots;
    }

    /**
     * Expand the hierarchy from the given roots
     */
    public void expand() {
        DisplayHierarchyContext context = initialiseContext();

        while (!context.completed()) {
            DisplayHierarchyNode node = context.queue().remove();
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
    protected void expandNode( DisplayHierarchyContext context, DisplayHierarchyNode node ) {
        List<PropertyValue> arcs = node.rdfNode().getDisplayProperties();
        Set<PropertyPath> paths = node.isRoot() ? context.basePaths() : node.explicitPaths();

        for (PropertyValue s: arcs) {
            Property p = s.getProp().toProperty( s.getProp() );
            PropertyPath pathTo = node.pathTo().append( null, p.getURI(), shortNameRenderer );

            for (RDFNodeWrapper childNode: s.getValues()) {
                DisplayHierarchyNode child = new DisplayHierarchyNode( pathTo, node, context.wrap( childNode ) );

                Set<PropertyPath> matchingPaths = matchingPaths( context, p, paths );
                child.explicitPaths().addAll( matchingPaths );

                if (!child.isLeaf( context )) {
                    context.queue().add( child );
                }

                context.see( child.rdfNode() );
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
     * @return A sub-set of <code>paths</code>, possibly empty, which start with the
     *         given property or <code>*</code>
     */
    protected Set<PropertyPath> matchingPaths( DisplayHierarchyContext context, Property p, Set<PropertyPath> paths ) {
        Set<PropertyPath> matching = new HashSet<PropertyPath>();

        for (PropertyPath path: paths) {
            if (path.beginsWith( p, shortNameRenderer )) {
                matching.add( path );
            }
        }

        return matching;
    }

    /** @return A new context bookkeeping object */
    protected DisplayHierarchyContext initialiseContext() {
        DisplayHierarchyContext context = new DisplayHierarchyContext();

        context.setPage( page );

        context.queue().addAll( roots );
        for (DisplayHierarchyNode n: roots) {
            context.see( n.rdfNode );
        }
        context.setBasePropertyPaths( new HashSet<PropertyPath>( page.currentPropertyPaths() ) );

        return context;
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /**
     * A collection of the state information we keep while expanding the display hierarchy
     */
    public class DisplayHierarchyContext {
        /** A queue of the non-expanded nodes */
        private Queue<DisplayHierarchyNode> queue = new ArrayDeque<DisplayHierarchyNode>();

        /** A list of the resource URIs we have seen */
        private Set<Resource> seen = new HashSet<Resource>();

        /** The base set of property paths we have to deal with */
        private Set<PropertyPath> basePaths;

        /** The current page */
        private Page page;

        /** @return The current hierarchy node queue */
        public Queue<DisplayHierarchyNode> queue() {
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

        /** @return A wrapped version of the given RDF node */
        public DisplayRdfNode wrap( RDFNode n ) {
            return new DisplayRdfNode( this.page, n );
        }

        /** @return A wrapped version of the given RDF node */
        public DisplayRdfNode wrap( RDFNodeWrapper n ) {
            return new DisplayRdfNode( this.page, n );
        }

        /** Set the model wrapper */
        public void setPage( Page page ) {
            this.page = page;
        }
    }

}
