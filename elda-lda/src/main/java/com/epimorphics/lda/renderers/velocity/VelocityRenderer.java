/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers.velocity;

import java.io.*;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.IncludeNotFound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.renderers.common.*;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.*;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.*;

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

    private static final Logger log = LoggerFactory.getLogger( VelocityRenderer.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    private final MediaType mt;
    private final Resource configRoot;
    private final Mode prefixMode;
    private APIEndpoint endpoint;
    private ShortnameService shortNameService;

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
     * @param root The configuration root resource
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

    /**
     * A <code>VelocityRendering</code> is essentially a closure of the various state
     * variables that are inputs into a rendering via the <code>render</code>
     * method.
     */
    static class VelocityRendering
    implements BytesOut
    {

        /***********************************/
        /* Constants                       */
        /***********************************/

        /** The default place we look for Velocity files */
        public static final String DEFAULT_VELOCITY_ROOT_PATH = "/velocity/";

        /** The configuration parameter which sets an alternative location for Velocity templates etc */
        public static final String VELOCITY_ROOT_CONFIG_PARAM = "_velocityRoot";

        /** Name of environment var to check for a velocity root directory */
        public static final String VELOCITY_ROOT_ENV_VAR = "VELOCITY_ROOT";

        /** Name of the properties file */
        public static final String VELOCITY_PROPERTIES_FILE = "velocity.properties";

        /***********************************/
        /* Instance variables              */
        /***********************************/

        private VelocityRenderer vr;
        private Bindings bindings;
        private APIResultSet results;

        /***********************************/
        /* Constructors                    */
        /***********************************/

        /**
         * Construct a Velocity rendering closure
         * @param b Current bindings
         * @param rs Current result set
         * @param vr Reference to the Velocity renderer object, which is also a container for some of the
         * configuration information
         */
        public VelocityRendering( Bindings b, APIResultSet rs, VelocityRenderer vr ) {
            this.bindings = b;
            this.results = rs;
            this.vr = vr;

            coerceAllMetadata();
        }

        /***********************************/
        /* External signature methods      */
        /***********************************/

        /**
         * Write the rendered output to the output stream, and side-effect the given
         * <code>Times</code> object to record duration and output counts.
         * @param times Record used to capture time and size information on output
         * @param os The output stream to write to
         */
        @Override
        public void writeAll( Times times, OutputStream os ) {
            CountStream cos = null;

            try {
                long base = System.currentTimeMillis();
                cos = new CountStream( os );
                render( os );
                StreamUtils.flush( os );
                times.setRenderedSize( cos.size() );
                times.setRenderDuration( System.currentTimeMillis() - base, vr.suffix() );
            }
            catch (Exception e) {
                log.warn( e.getMessage(), e );
                throw new EldaException( "Exception during Velocity rendering", e.getMessage(), 0, e );
            }
            finally {
                if (cos != null) {
                    try {
                        cos.close();
                    }
                    catch (IOException e) {
                        log.warn( "Failed to close count stream: " + e.getMessage(), e );
                    }
                }
            }
        }

        /***********************************/
        /* Internal implementation methods */
        /***********************************/

        /**
         * Irrespective of what is stated in the request URL, we ensure that we see
         * all of the available metadata for this endpoint
         */
        protected void coerceAllMetadata() {
            this.results.includeMetadata( MetadataOptions.allOptions().asArray() );
        }

        /**
         * Render the top-level template, given the result set and other state stored
         * in this rendering closure.
         *
         * @param os The output stream to write to
         */
        protected void render( OutputStream os ) {
            VelocityEngine ve = createVelocityEngine();
            VelocityContext vc = createVelocityContext( this.bindings );

            Template t = ve.getTemplate( vr.templateName() );

            try {
                Writer w = new OutputStreamWriter( os, "UTF-8" );
                t.merge( vc,  w );
                w.close();
            }
            catch (UnsupportedEncodingException e) {
                throw new BrokenException( e );
            }
            catch (IOException e) {
                throw new WrappedException( e );
            }
        }

        /**
         * Create a Velocity engine instance, and initialise it with properties
         * loaded from the <code>velocity.properties</code> file.
         * @return A new Velocity engine
         */
        protected VelocityEngine createVelocityEngine() {
            String velocityRoot = velocityRoot(bindings);

            Properties p = getProperties( velocityRoot );
            VelocityEngine ve = new VelocityEngine();

            ve.init( p );

            return ve;
        }

        /** @return The root directory where we expect to find templates and <code>velocity.properties</code> */
        protected String velocityRoot( Bindings b ) {
            String rootPath = b.getAsString( VELOCITY_ROOT_CONFIG_PARAM, defaultVelocityRoot() );

            String rootURL = b.pathAsURL( rootPath ).toString();
            return (rootURL.endsWith( "/" )) ? rootURL : (rootURL + "/");
        }

        /** @return The default Velocity root directory, which may set by an environment variable */
        protected String defaultVelocityRoot() {
            String envRoot = null;
            try {
                envRoot = System.getenv( VELOCITY_ROOT_ENV_VAR );
            }
            catch (SecurityException ignore) {
                // not allowed to read the environment, no biggie
            }

            return (envRoot == null) ? DEFAULT_VELOCITY_ROOT_PATH : envRoot;
        }

        /** @return A properties object containing the configuration properties for this velocity instance */
        protected Properties getProperties( String velocityRoot ) {
            Properties p = new Properties();

            loadPropertiesFile( velocityRoot, p );
            setDynamicProperties( velocityRoot, p );

            return p;
        }

        /**
         * Set additional properties that are calculated based on current state
         * @param velocityRoot
         * @param p
         */
        protected void setDynamicProperties( String velocityRoot, Properties p ) {
            p.setProperty( "url.resource.loader.root", velocityRoot );

            if (velocityRoot.startsWith( "file:" )) {
                String velocityDir = velocityRoot.replace( "file:", "" );
                String path = p.getProperty( "file.resource.loader.path" );

                path = ((path == null) ? "" : (path + ", ")) + velocityDir;

                p.setProperty( "file.resource.loader.path", path );
            }
        }

        /**
         * Load the <code>velocity.properties</code> file from the Velocity root, if it exists.
         * @param velocityRoot
         * @param p
         */
        protected void loadPropertiesFile( String velocityRoot, Properties p ) {
            InputStream is = EldaFileManager.get().open( velocityRoot + VELOCITY_PROPERTIES_FILE );

            if (is != null) {
                try {
                    p.load( is );
                }
                catch (IOException e) {
                    log.warn( "IO exception while reading properties: " + e.getMessage(), e );
                    throw new WrappedIOException( e );
                }
                finally {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        log.warn( "IO exception while closing properties input stream: " + e.getMessage(), e );
                        throw new WrappedIOException( e );
                    }
                }
            }
        }

        /** @return A new velocity context containing bindings that we will use to render the results */
        protected VelocityContext createVelocityContext( Bindings binds ) {
            Page page = initialisePage();
            DisplayHierarchy dh = initialiseHierarchy( page );

            VelocityContext vc = new VelocityContext();
            addStandardVariables( vc, page, dh );
            addBindingsToContext( vc, binds );
            addContextSelfReference( vc );
            addEventHandlers( vc );
            addTools( vc );

            return vc;
        }

        /** Add the standard variables to the context */
        protected void addStandardVariables( VelocityContext vc, Page page, DisplayHierarchy dh ) {
            vc.put( "page", page );
            vc.put( "hierarchy", dh );
            vc.put( "renderer", this.vr );
        }

        /** Add the Elda bindings as context variables */
        protected void addBindingsToContext( VelocityContext vc, Bindings binds ) {
            for (String key: binds.keySet()) {
                vc.put( key, binds.get( key ) );
            }
        }

        /** Attach the event handlers we want */
        protected void addEventHandlers( VelocityContext vc ) {
            EventCartridge ec = new EventCartridge();

            ec.addEventHandler( new IncludeNotFound() );

            vc.attachEventCartridge( ec );
        }

        /** Add the Velocity Contex itself, as a debugging aide */
        protected void addContextSelfReference( VelocityContext vc ) {
            vc.put( "vcontext", vc );
        }

        /** Add generic tools to the Velocity context */
        protected void addTools( VelocityContext vc ) {
            vc.put( "esc", new StringEscapeUtils() );
            vc.put( "log", log );
        }

        /**
         * Initialise the display hierarchy, which unrolls the RDF graph into a displayable tree
         * @param page The current page object
         * @return The display hierarchy
         */
        protected DisplayHierarchy initialiseHierarchy( Page page ) {
            page.initialiseShortNameRenderer( vr.shortNameService );
            DisplayHierarchy dh = new DisplayHierarchy( page );
            dh.expand();
            return dh;
        }

        /**
         * @return A new Page object providing access to the page of results
         */
        protected Page initialisePage() {
            ResultsModel rm = new ResultsModel( results );
            return rm.page();
        }
    }
}