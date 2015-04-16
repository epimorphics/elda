/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Denotes a link to an adjacent or related endpoint, for example with (or
 * without) an additional filter or sort clause.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class Link
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( Link.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The title of the link */
    private String title;

    /** The address we are linking to */
    private EldaURL url;

    /** Additional display hints; could be used for CSS classes etc */
    private List<String> hints = new ArrayList<String>();

    /***********************************/
    /* Constructors                    */
    /***********************************/

    public Link( String title, EldaURL url, String hint ) {
        this.title = title;
        this.url = url;
        if (hint != null) {
            this.hints.add( hint );
        }
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    public String title() {
        return title;
    }

    public EldaURL url() {
        return url;
    }

    public List<String> hints() {
        return hints;
    }

    @Override
    public String toString() {
        return toHTMLString( null );
    }

    /**
     * Return this link in HTML markup form.
     * @param elem If non-null, denotes a element to use to bracket the link, e.g. <code>li</code>
     * @return The contents of this link as an HTML <code>a</code> element
     */
    public String toHTMLString( String elem ) {
        StringBuffer buf = new StringBuffer();
        if (elem != null) {
            buf.append( "<" + elem + ">" );
        }

        buf.append( "<a " );
        buf.append( "href='" + url().toString() + "' " );
        buf.append( "class='" + StrUtils.strjoin( " ", hints()  ) + "' " );
        buf.append( ">" );
        buf.append( title() );
        buf.append( "</a>" );

        if (elem != null) {
            buf.append( "</" + elem + ">" );
        }

        return buf.toString();
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}

