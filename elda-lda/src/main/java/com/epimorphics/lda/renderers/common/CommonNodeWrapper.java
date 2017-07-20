/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.epimorphics.rdfutil.ModelWrapper;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;



/**
 * This is an extension to {@link com.epimorphics.rdfutil.RDFNodeWrapper} from the
 * Epimorphics general library, simply to allow some local additions to the functional
 * capabilities of RDFNodeWrapper. In time, many or all of these methods may migrate to the
 * library version.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class CommonNodeWrapper
extends RDFNodeWrapper
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

    /** The page this resource is attached to, if any */
    private Page page;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public CommonNodeWrapper( ModelWrapper mw, RDFNode node ) {
        super( mw, node );
    }

    /**
     * Construct a common node wrapper that is attached to a particular page.
     *
     * @param page The page object
     * @param node The wrapped node
     */
    public CommonNodeWrapper( Page page, RDFNode node ) {
        super( page.getModelW(), node );
        this.page = page;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * @return The page object this resource is attached to, or null
     */
    public Page page() {
        return this.page;
    }

    /**
     * Return the int value of a property <code>p</code> of this resource, or return a default
     * value. The default value is returned when: this node is not a resource,
     * this resource does not have a property <code>p</code>, or the value of <code>p</code>
     * is not an integer.
     *
     * @param p A property, specified as a property object, URI or curie
     * @param def The default value
     * @return The integer value of p, or the default value
     */
    public int getInt( Object p, int def ) {
        int v = def;

        if (isResource()) {
            com.epimorphics.rdfutil.RDFNodeWrapper n = getPropertyValue( p );
            if (n != null && n.isLiteral()) {
                try {
                    v = n.asLiteral().getInt();
                }
                catch (DatatypeFormatException e) {
                    // ignore this error - we return the default value
                }
            }
        }

        return v;
    }

    /**
     * Return the resource value of the given property, or null.
     * @param p A property, specified as a property object, URI or curie
     * @return The value of a <code>p</code> property of this node if it is a resource.
     * If this node is not a resource, or does not have at least one <code>p</code>
     * property, return null
     */
    public Resource getResource( Object p ) {
        Resource r = null;

        if (isResource()) {
            com.epimorphics.rdfutil.RDFNodeWrapper n = getPropertyValue( p );
            if (n != null && n.isResource()) {
                r = n.asResource();
            }
        }

        return r;
    }

    /**
     * @return True if this node is a literal, and has an XML literal as its value
     */
    public boolean isXmlLiteral() {
        if (isLiteral()) {
            RDFDatatype typ = asLiteral().getDatatype();
            return (typ != null) && XMLLiteralType.theXMLLiteralType.equals( typ );
        }
        else {
            return false;
        }
    }

    /**
     * @return A name for the resource. By preference, this will be a label property
     * of the resource. Otherwise, a curie or localname will be used if no naming property is found.
     * Special cases for displaying names of nodes with no labels in Elda:
     * <ul>
     * <li>if the name would otherwise be empty, and the URI ends with `.../` or `.../-`,
     * use the preceding path segment</li>
     * <li>if the name would otherwise be empty, and the node is a bNode, use the anon-ID</li>
     * <li>if the name would otherwise be empty, use the full URI</li>
     * </ul>
     */
    @Override
    public String getName() {
        String name = super.getName();

        if (name.isEmpty() || name.matches("^\\s*$") || name.equals( getURI() )) {
            if (isAnon()) {
                name = asRDFNode().asResource().getId().toString();
            }
            else {
                Matcher match = lnmatch.matcher(getURI());
                if (match.matches()) {
                    name = match.group(1);
                }
                else {
                    name = getURI();
                }
            }
        }

        return name;
    }

//    static final Pattern lnmatch = Pattern.compile("([^#/]*[/#]\\-?)$");
    static final Pattern lnmatch = Pattern.compile(".*[/#]([^#/]+[/#]-?)$");

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

