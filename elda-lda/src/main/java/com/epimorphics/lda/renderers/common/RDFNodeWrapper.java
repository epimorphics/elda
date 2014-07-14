/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;

import com.epimorphics.rdfutil.ModelWrapper;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.rdf.model.*;



/**
 * This is an extension to {@link com.epimorphics.rdfutil.RDFNodeWrapper} from the
 * Epimorphics general library, simply to allow some local additions to the functional
 * capabilities of RDFNodeWrapper. In time, many or all of these methods may migrate to the
 * library version.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class RDFNodeWrapper extends com.epimorphics.rdfutil.RDFNodeWrapper
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

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public RDFNodeWrapper( ModelWrapper mw, RDFNode node ) {
        super( mw, node );
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

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

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

