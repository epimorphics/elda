/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;

import java.util.List;

import com.epimorphics.rdfutil.PropertyValue;
import com.epimorphics.rdfutil.RDFNodeWrapper;




/**
 * Encapsulates a strategy pattern choice of the ordering of predicates
 * with a common subject resource.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public interface PropertyOrderingStrategy
{
    /**
     * Order the triples whose common subject is <code>subject<code>
     * according to some regular principle, such as lexical sort by
     * the label of the predicate.
     *
     * @param subject The common subject resource
     * @return A list of the triples whose subject resource is <code>subject</code>,
     * sorted into a desired order.
     */
    List<PropertyValue> orderProperties( RDFNodeWrapper subject );
}

