/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers.velocity;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.MediaType;
import com.epimorphics.util.RDFUtils;
import com.hp.hpl.jena.rdf.model.Resource;

public class VelocityRenderer
implements Renderer
{

    /***********************************/
    /* Constants                       */
    /***********************************/

    public static final String DEFAULT_FORMAT = "html";
    public static final String DEFAULT_METADATA_OPTIONS = "bindings,formats,versions,execution";

    /** The default page template which will define the overall presentation unless
     *  a template is named in the Elda configuration */
    public static final String DEFAULT_TEMPLATE = "index.vm";

    /***********************************/
    /* Static variables                */
    /***********************************/

    @SuppressWarnings( "unused" )
    private static final Logger log = LoggerFactory.getLogger( VelocityRenderer.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    private final MediaType mt;
    private final Resource configRoot;
    private final Mode prefixMode;
    private APIEndpoint endpoint;
    ShortnameService shortNameService;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /**
     * Construct a new Velocity renderer
     * @param mt The media type that this renderer returns, usually <code>text/html</code>
     * @param ep The API endpoint configuration
     * @param config The RDF resource that is the root of the API configuration declaration
     * @param prefixMode The required prefix mode, or <code>null</code> for default
     * @param sns The current short name service
     */
    public VelocityRenderer( MediaType mt, APIEndpoint ep, Resource config, Mode prefixMode, ShortnameService sns ) {
        this.mt = mt;
        this.endpoint = ep;
        this.prefixMode = (prefixMode == null) ? Mode.PreferLocalnames : prefixMode;
        this.configRoot = config;
        this.shortNameService = sns;
    }

    /***********************************/
    /* External signature methods      */
    /***********************************/

    /** @return The media type. TODO I'd like to understand why the bindings are irrelevant */
    @Override
    public MediaType getMediaType( Bindings irrelevant ) {
        return mt;
    }

    /** @return The preferred suffix that this renderer was initialised with */
    @Override
    public String getPreferredSuffix() {
        return suffix();
    }

    /** @return The prefix {@link Mode} that this renderer was initialised with */
    @Override
    public Mode getMode() {
        return prefixMode;
    }

    /** @return The root configuration resource */
    public Resource configRoot() {
        return configRoot;
    }

    /** @return The suffix for the generated page */
    public String suffix() {
        return RDFUtils.getStringValue( configRoot(), API.name, DEFAULT_FORMAT );
    }

    /** @return The name of the outer template to use */
    public String templateName() {
        return RDFUtils.getStringValue( configRoot(), ELDA_API.velocityTemplate, DEFAULT_TEMPLATE );
    }

    /** @return The endpoint description object */
    public APIEndpoint endpoint() {
        return endpoint;
    }

    /** @return The shortname service instance */
    public ShortnameService shortNameService() {
        return shortNameService;
    }

    /**
     * Return the configured metadata options, or, if not specified in the API
     * configuration, the default options.
     *
     * @return The metadata options for this renderer
     */
    public MetadataOptions metadataOptions() {
        return new MetadataOptions( configRoot(), DEFAULT_METADATA_OPTIONS );
    }

    /**
     * Render the given result set
     * @param t TODO As far as I can tell, this parameter is redundant
     * @param b The variable bindings determined by the Elda query
     * @param termBindings Ignored
     * @param results The query results
     */
    @Override
    public Renderer.BytesOut render( Times t, Bindings b,
                                     Map<String, String> termBindings,
                                     APIResultSet results ) {
        return new VelocityRendering( b, results, this );
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/


    /***********************************/
    /* Inner class definitions         */
    /***********************************/
}