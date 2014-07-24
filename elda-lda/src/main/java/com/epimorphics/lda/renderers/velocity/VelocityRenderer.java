/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2014 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.renderers.velocity;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.core.APIResultSet.MergedModels;
import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.support.Times;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;

public class VelocityRenderer
implements Renderer
{

    /***********************************/
    /* Constants                       */
    /***********************************/

    public static final String DEFAULT_FORMAT = "html";
    public static final String DEFAULT_METADATA_OPTIONS = "bindings,formats,versions,execution";
    public static final String DEFAULT_TEMPLATE = "page-shell.vm";

    /***********************************/
    /* Static variables                */
    /***********************************/

    private static final Logger log = LoggerFactory.getLogger( VelocityRenderer.class );

    /***********************************/
    /* Instance variables              */
    /***********************************/

    final MediaType mt;
    final Resource configRoot;
    private final Mode prefixMode;
    private APIEndpoint endpoint;
    private ShortnameService shortNameService;

    /***********************************/
    /* Constructors                    */
    /***********************************/

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
     * @param termBindings Shortname bindings
     * @param results The query results
     */
    @Override
    public Renderer.BytesOut render( Times t, Bindings b,
                                     Map<String, String> termBindings,
                                     APIResultSet results ) {
        return new VelocityRendering( b, termBindings, results, this );
    }


    /***********************************/
    /* Internal implementation methods */
    /***********************************/


    /***********************************/
    /* Inner class definitions         */
    /***********************************/

    /**
     * A <code>VelocityRendering</code> is essentially a closure of the various state
     * variables that provide considerations when rendering into a <code>render</code>
     * method.
     */
    static class VelocityRendering
    implements BytesOut
    {
        private Bindings bindings;
        private APIResultSet results;
        private VelocityRenderer vr;
        private Map<String,String> termBindings;

        public VelocityRendering( Bindings b, Map<String,String> tb, APIResultSet rs, VelocityRenderer vr ) {
            this.bindings = b;
            this.termBindings = tb;
            this.results = rs;
            this.vr = vr;
        }

        /** Internal implementation methods */

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

        protected void render( OutputStream os ) {
            VelocityEngine ve = createVelocityEngine();
            Resource thisPage = results.getRoot();
            MergedModels mm = results.getModels();
            Model m = mm.getMergedModel();
            IdMap ids = new IdMap();
            ShortNames names = Help.getShortnames( m );
            boolean isItemEndpoint = thisPage.hasProperty(FOAF.primaryTopic);
            boolean isListEndpoint = !isItemEndpoint;
            WrappedNode.Bundle b = new WrappedNode.Bundle( names,  ids );
            List<WrappedNode> itemised = WrappedNode.itemise( b, results.getResultList() );
        //
            Map<String, String> filters = new HashMap<String, String>();
            for (String name: bindings.parameterNames()) {
                if (name.charAt(0) != '_')
                    filters.put(name, bindings.get(name).spelling());
            }
        //
            VelocityContext vc = new VelocityContext();
            WrappedNode wrappedPage = new WrappedNode( b, thisPage );
            vc.put( "type_suffix", vr.suffix() );
            vc.put( "thisPage", wrappedPage );
            vc.put( "isItemEndpoint", isItemEndpoint );
            vc.put( "isListEndpoint", isListEndpoint );
//            if (isItemEndpoint) vc.put( "primaryTopic", topicOf(b, thisPage) );
            vc.put( "ids",  ids );
            vc.put( "names", names );
            vc.put( "formats", Help.getFormats( m ) );
            vc.put( "views", Help.getViews( m ) );
            vc.put( "items", itemised );
            vc.put( "meta", Help.getMetadataFrom( names, ids, m ) );
            vc.put( "vars", Help.getVarsFrom( names, ids, m ) );
            vc.put( "filters", filters );
        //
            Template t = ve.getTemplate( vr.templateName() );
            try {
                Writer w = new OutputStreamWriter( os, "UTF-8" );
                t.merge( vc,  w );
                w.close();
            } catch (UnsupportedEncodingException e) {
                throw new BrokenException( e );
            } catch (IOException e) {
                throw new WrappedException( e );
            }
        }

        protected VelocityEngine createVelocityEngine() {
            String templateRoot = getTemplateRoot(bindings);
            String propertiesName = templateRoot + "/velocity.properties";
            Properties p = getProperties( propertiesName );
            VelocityEngine ve = new VelocityEngine();
            if (p.isEmpty()) {
                log.debug( "using default velocity properties." );
            //
                ve.setProperty( "macro.provide.scope.control", true );
                ve.setProperty( "foreach.provide.scope.control", true );
                ve.setProperty( "runtime.references.strict", "true" );
//              ve.setProperty( "resource.loader",  "class" );
//              ve.setProperty( "class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );
            //
                ve.setProperty( "file.resource.loader.path", templateRoot );
                ve.setProperty( "file.resource.loader.cache", "true" );
                ve.setProperty( "file.resource.loader.modificationCheckInterval", "5" );
            //
                ve.setProperty( "resource.loader", "file, class, url" );
            //
                ve.setProperty( "url.resource.loader.class", "org.apache.velocity.runtime.resource.loader.URLResourceLoader" );
                ve.setProperty( "url.resource.loader.root", templateRoot );
                ve.setProperty( "url.resource.loader.cache", true );
                ve.setProperty( "url.resource.loader.modificationCheckInterval", "20" );
            //
//              ve.setProperty( "class.resource.loader.cache", false );
//              ve.setProperty( "velocimacro.library.autoreload", true );
            //
                ve.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
                ve.setProperty("runtime.log.logsystem.log4j.category", "velocity");
                ve.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
            } else {
                log.info( "loaded properties file " + propertiesName );
            }
            ve.init();
            return ve;
        }

        protected String getTemplateRoot(Bindings b) {
            String defaultRoot = b.getAsString("_velocityRoot", "/vm/");
            URL u = b.pathAsURL(defaultRoot);
            return u.toString() + "/";
        }

        protected Properties getProperties( String fileName ) {
            Properties p = new Properties();
            InputStream is = EldaFileManager.get().open( fileName );
            if (is != null) loadNicely( p, is );
            return p;
        }

        protected void loadNicely(Properties p, InputStream is) {
            try { p.load( is ); is.close(); }
            catch (IOException e) { throw new WrappedIOException( e ); }
        }




    }


}