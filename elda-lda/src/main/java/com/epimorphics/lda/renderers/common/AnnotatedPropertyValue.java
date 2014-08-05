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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.rdfutil.*;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * An extension to the library class {@link PropertyValue}, which allows us to annotate
 * property values (for example for odd and even rows, or to note the last pair in a
 * sequence).
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class AnnotatedPropertyValue
extends PropertyValue
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( AnnotatedPropertyValue.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** List of annotations for this pair */
    private List<String> annotations = new ArrayList<String>();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public AnnotatedPropertyValue(RDFNodeWrapper prop) {
        super( prop );
    }

    public AnnotatedPropertyValue(ModelWrapper modelw, Property prop) {
        super( modelw, prop );
    }

    public AnnotatedPropertyValue(RDFNodeWrapper prop, RDFNodeWrapper value) {
        super( prop, value );
    }

    public AnnotatedPropertyValue( PropertyValue pv ) {
        super( pv.getProp() );
        for (RDFNodeWrapper v: pv.getValues()) {
            addValue( v );
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Add an annotation to the property value pair
     * @param annotation
     */
    public void annotate( String annotation ) {
        annotations.add( annotation );
    }

    /**
     * @return The list of annotations, which may be emtpy but will not be null
     */
    public List<String> annotations() {
        return this.annotations;
    }

    /**
     * @return The annotations joined into one string, which may be empty but will not be null
     */
    public String annotationsString() {
        return StringUtils.join( annotations(), " " );
    }

    /** Sort values before returning */
    @Override
    public List<RDFNodeWrapper> getValues() {
        Collections.sort( values, new Comparator<RDFNodeWrapper>() {
            @Override
            public int compare( RDFNodeWrapper o1, RDFNodeWrapper o2 ) {
                return o1.getLexicalForm().compareTo( o2.getLexicalForm() );
            }
        });

        return values;
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

