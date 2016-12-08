/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.EldaException;

/**
 * A decorator for the URL of an Elda page, which also provides mechanisms for
 * manipulating the URL elements to produce related pages.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class EldaURL
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    /** The ways we can change parameter values to create a related URL */
    enum OPERATION { ADD, REMOVE, SET }

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( value = "unused" )
    private static final Logger log = LoggerFactory.getLogger( EldaURL.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    /** The base URL */
    private URI uri;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct an Elda page URL by parsing the given string as a URI
     * @param url String denoting the page URL
     * @exception EldaException if the given string is not a legal URI
     */
    public EldaURL( String url )
    {
        try {
            uri = new URI( url );
        }
        catch (URISyntaxException e) {
            throw new EldaException( "Failed to parse Elda page URL", e.getMessage(), EldaException.SERVER_ERROR, e );
        }
    }

    /**
     * Construct an Elda page URL from the given URI object
     * @param url
     */
    public EldaURL( URI url ) {
        uri = url;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /**
     * Create a new EldaURL by applying an operation to this URL to add, remove or set
     * a parameter value.
     * @param op The operation as a string
     * @param paramName The name of the parameter (e.g. _properties)
     * @param paramValue The value to be used with the operation
     * @return A new EldaURL object
     * @see #withParameter(OPERATION, String, String)
     */
    public EldaURL withParameter( String op, String paramName, String paramValue ) {
        return withParameter( OPERATION.valueOf( op ), paramName, paramValue );
    }

    /**
     * Create a new EldaURL by applying an operation to this URL to add, remove or set
     * a parameter value.
     * @param op The operation: ADD ensures that the parameter has the value, REMOVE ensures that the
     * parameter does not have the value, and SET ensures that the parameter has only
     * that value
     * @param paramName The name of the parameter (e.g. _properties)
     * @param paramValue The value to be used with the operation
     * @return A new EldaURL object
     */
    public EldaURL withParameter( OPERATION op, String paramName, String paramValue ) {
        Map<String, URLParameterValue> queryParameters = parseQueryParameters(),
                                       revisedParameters = new HashMap<String, URLParameterValue>();

        boolean acted = false;
        for (Map.Entry<String, URLParameterValue> entry: queryParameters.entrySet()) {
            String p = entry.getKey();
            URLParameterValue v = entry.getValue();

            if (paramName.equals( p )) {
                for (String pValue: StringUtils.split( paramValue, "," )) {
                    v = v.perform( op, pValue );
                    acted = true;
                }
            }

            updateParameters( revisedParameters, p, v );
        }

        if (!acted) {
            URLParameterValue upv = new URLParameterValue().perform( op, paramValue );
            updateParameters( revisedParameters, paramName, upv );
        }

        return generateURL( revisedParameters );
    }

    /** @return True if the URL already has the given parameter/value pair */
    public boolean hasParameter( String paramName, String paramValue ) {
        URLParameterValue v = getParameter( paramName );
        boolean contains = false;

        if (v != null) {
            if (paramValue.contains( "," )) {
                contains = true;

                for (String pValue : StringUtils.split( paramValue, "," )) {
                    contains = contains && v.contains( pValue );
                }
            }
            else {
                contains = v.contains( paramValue );
            }
        }

        return contains;
    }

    /** @return The value of the given parameter to the URL, or null */
    public URLParameterValue getParameter( String paramName ) {
        return parseQueryParameters().get( paramName );
    }

    /**
     * Return a list of URL parameters, possibly excluding those internal
     * use parameters (i.e. that start with an underscore character).
     * @param external If true, remove internal-use parameters
     * @return A non-null but possibly empty list of current URL parameter values
     */
    public List<NamedParameterValue> getParameters( boolean external ) {
        List<NamedParameterValue> params = new ArrayList<EldaURL.NamedParameterValue>();

        for (Map.Entry<String, URLParameterValue> entry: parseQueryParameters().entrySet()) {
            if (!(external && isInternalParameterName( entry.getKey() ))) {
                params.add( new NamedParameterValue( entry.getKey(), entry.getValue() ) );
            }
        }

        Collections.sort( params );
        return params;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    /** @return The encapsulated URI object */
    public URI getUri() {
        return uri;
    }
    
    /**
     * Return the parent URL, which we obtain by removing the trailing path
     * segment of the URL path. If a suitable path segment to remove cannot be
     * identified, return self.
     * @return A URL for the parent of this URL
     */
    public EldaURL parentURL() {
        String path = getUri().getPath();
        Pattern pat = Pattern.compile( "\\A.*(/[^/]*)\\Z" );
        Matcher mat = pat.matcher( path );
        if (mat.matches()) {
            String lastSegment = mat.group( 1 );
            URI parentURI = URI.create( getUri().toString().replace( lastSegment, "" ) );
            return new EldaURL( parentURI );
        }
        else {
            return this;
        }
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    /**
     * Use the given value to update the parameters. If <code>value</code> is empty, remove
     * the <code>paramName</code> key.
     * @param parameters The parameters map to be updated
     * @param paramName The parameter key
     * @param value The revised value
     */
    protected void updateParameters( Map<String, URLParameterValue> parameters, String paramName, URLParameterValue value ) {
        if (value.isEmpty()) {
            parameters.remove( paramName );
        }
        else {
            parameters.put( paramName, value );
        }
    }

    /** Parse the query string, if there is one, into a structure we can manipulate */
    // if there's no =value, use the empty url parameter value
    protected Map<String, URLParameterValue> parseQueryParameters() {
        Map<String, URLParameterValue> queryParameters = new HashMap<String, EldaURL.URLParameterValue>();

        if (uri.getQuery() != null) {
            String[] pairs = StringUtils.split( uri.getQuery(), "&" );
            for (String pair: pairs) {
                String[] pv = StringUtils.split( pair, "=" );
                URLParameterValue u = pv.length < 2 
                	? new URLParameterValue("") 
                	: new URLParameterValue(pv[1])
                	;
                queryParameters.put( pv[0], u );
            }
        }

        return queryParameters;
    }

    /** Turn the parsed query parameters back into a string */
    protected String queryString( Map<String, URLParameterValue> qParams ) {
        List<String> params = new ArrayList<String>();

        for (Map.Entry<String, URLParameterValue> entry: qParams.entrySet()) {
            params.add( entry.getKey() + "=" + entry.getValue() );
        }

        String qString = StringUtils.join( params, "&" );
        return qString.isEmpty() ? null : qString;
    }

    /**
     * Generate a URL that has the same structure as this URL, except for the given parameters.
     * @param parameters Parameters of the new URL object, as a map
     * @return A new URL object
     */
    protected EldaURL generateURL( Map<String, URLParameterValue> parameters ) {
        try {
            URI u = new URI( uri.getScheme(), uri.getUserInfo(),
                             uri.getHost(), uri.getPort(),
                             uri.getPath(),
                             queryString( parameters ),
                             uri.getFragment() );
            return new EldaURL( u );
        }
        catch (URISyntaxException e) {
            throw new EldaException( "Failed to generate new EldaURL", e.getMessage(), EldaException.SERVER_ERROR, e );
        }
    }

    /** @return True if the parameter name denotes an internal name, starting with an underscore */
    protected boolean isInternalParameterName( String name ) {
        return name.startsWith( "_" );
    }


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /** A parameter value in the URL construction overall may have more than one value, with a comma separator */
    public static class URLParameterValue
    {
        /** A list of the values of the parameter */
        private List<String> values = new ArrayList<String>();

        /** Construct a new empty parameter value */
        public URLParameterValue() {
            // nothing to do ..
        }

        /** Construct a new parameter value by parsing the given string representation */
        public URLParameterValue( String values ) {
            for (String v: StringUtils.split( values, "," )) {
                this.values.add( v );
            }
        }

        /** Clone existing values list */
        protected URLParameterValue( URLParameterValue existing ) {
            this.values.addAll( existing.values );
        }

        /** @return True if this parameter has no values */
        public boolean isEmpty() {
            return values.isEmpty();
        }

        /** @return A new parameter value which is guaranteed to include the given term */
        public URLParameterValue with( String value ) {
            URLParameterValue upv = new URLParameterValue( this );
            if (!upv.values.contains( value )) {
                upv.values.add( value );
            }
            return upv;
        }

        /** @return A new parameter value which does not include the given term */
        public URLParameterValue without( String value ) {
            URLParameterValue upv = new URLParameterValue( this );
            upv.values.remove( value );
            return upv;
        }

        /** @return A new parameter value which has the given term as its only value */
        public URLParameterValue setValue( String value ) {
            return new URLParameterValue( value );
        }

        /** @return A new parameter value based on performing the given operation */
        public URLParameterValue perform( OPERATION op, String value ) {
            switch (op) {
                case ADD: return with( value );
                case REMOVE: return without( value );
                case SET: return setValue( value );
                default:
                    // should be impossible!
                    throw new IllegalArgumentException();
            }
        }

        /** @return True if the list of values includes the given value */
        public boolean contains( String value ) {
            return values.contains( value );
        }

        /** @return A list of the values this parameter value object denotes */
        public List<String> values() {
            return values;
        }

        @Override
        public String toString() {
            return StringUtils.join( values, "," );
        }
    }


    /** A pair of parameter name and value */
    public static class NamedParameterValue
    extends URLParameterValue
    implements Comparable<NamedParameterValue>
    {
        private String name;

        public NamedParameterValue( String name, String values ) {
            super( values );
            this.name = name;
        }

        public NamedParameterValue( String name, URLParameterValue value ) {
            super( value );
            this.name = name;
        }

        public String name() {
            return this.name;
        }

        @Override
        public int compareTo( NamedParameterValue o ) {
            return name().compareTo( o.name() );
        }
    }
}

