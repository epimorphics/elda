/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.velocity;


import java.io.*;
import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.EventCartridge;
import org.apache.velocity.app.event.implement.IncludeNotFound;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.exceptions.VelocityRenderingException;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.renderers.Renderer.BytesOut;
import com.epimorphics.lda.renderers.common.*;
import com.epimorphics.lda.specs.MetadataOptions;
import com.epimorphics.lda.support.*;
import com.epimorphics.util.CountStream;
import com.epimorphics.util.StreamUtils;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

/**
 * A VelocityRendering captures the state required to render a particular request into
 * some output format using Velocity macros. There is one VelocityRendering object per
 * incoming request.
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class VelocityRendering
implements BytesOut
{

	/***********************************/
    /* Constants                       */
    /***********************************/
	
	/* poison string in case error occurs during streaming */
	static final String HTML_POISON = "\n<=>'<=>\"<=>\n";

    /** The default place we look for Velocity files */
    public static final String DEFAULT_VELOCITY_ROOT_PATH = "/velocity/";

    /** The configuration parameter which sets an alternative location for Velocity templates etc */
    public static final String VELOCITY_PATH_CONFIG_PARAM = "_velocityPath";

    /** Name of environment var to check for a velocity root directory */
    public static final String VELOCITY_PATH_ENV_VAR = "VELOCITY_PATH";

    /** Name of the properties file */
    public static final String VELOCITY_PROPERTIES_FILE = "velocity.properties";

    /** Velocity property for configuring loader path */
    public static final String VELOCITY_FILE_RESOURCE_LOADER_PATH = "file.resource.loader.path";

    /***********************************/
    /* Static variables                */
    /***********************************/

    private static final Logger log = LoggerFactory.getLogger( VelocityRendering.class );

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
            log.warn(ELog.message("%s (%s)", e.getMessage(), e ));
            throw new VelocityRenderingException();
        }
        finally {
            if (cos != null) {
                try {
                    cos.close();
                }
                catch (IOException e) {
                    log.warn(ELog.message("failed to close count stream: %s (%s)", e.getMessage(), e ));
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
        vc.put("_licenses", LicenceResource.revise(results.getLicences()));
        
        Template t = null;

        try {
            t = ve.getTemplate( vr.templateName() );
        }
        catch (ResourceNotFoundException e) {
			log.debug(ELog.message("could not find base template '%s'", vr.templateName()) );
            log.debug(ELog.message("current velocity path is '%s'", ve.getProperty( VELOCITY_FILE_RESOURCE_LOADER_PATH )));
            throw e;
        }

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
    public VelocityEngine createVelocityEngine() {
        List<String> velocityPath = expandVelocityPath( bindings );

        Properties p = getProperties( velocityPath );
        VelocityEngine ve = new VelocityEngine();

        ve.init( p );

        return ve;
    }

    /**
		expandedVelocityPath returns a list of paths/URLs where Velocity may
		search for its templates. The path is composed of
		
		<ul>
			<li>any components of the variable _velocityPath
			<li>the first component of /etc/elda/conf.d/{APP}/_error_pages if any
			<li>webapp/_error_pages
			<li>the velocity root default, currently /velocity/.
		</ul>
		
		This allows Elda Common to have error pages built-in that can be
		overridden in /etc/elda and in turn those can be over-ridden by
		entries in _velocityPath.
		
    	@return An array of expanded file paths or other URLs where we will search for Velocity assets
    */
    protected List<String> expandVelocityPath( Bindings b ) {
        List<String> roots = new ArrayList<>();
        String userRootPath = b.getAsString( VELOCITY_PATH_CONFIG_PARAM, null );
		
		String rootPath =
			(userRootPath == null ? "" : userRootPath + ",")
			+ etcPath()
			+ webappPath()
			+ defaultVelocityRoot()
			;
		
        for (String pathEntry: StringUtils.split( rootPath, "," )) {
            pathEntry = StringUtils.trim( pathEntry );
            String pathURL = b.pathAsURL( pathEntry).toString();
            roots.add( pathURL + (pathURL.endsWith( "/" ) ? "" : "/") );
        }
        log.debug(ELog.message("rootPath '%s'", rootPath));
        log.debug(ELog.message("complete expanded path '%s'", roots));
        return roots;
    }

    private String webappPath() {
		return "_error_pages/";
	}

	private String etcPath() {
		String context = bindings
			.getAsString("_rootPath", "NO_ROOTPATH")
			.replaceAll("/([^/]*)/.*", "$1")
			;
		String appPath = "/etc/elda/conf.d/REPLACE/_error_pages".replace("REPLACE", context);
		List<File> files = new Glob().filesMatching(appPath);		
		return (files.size() == 0 ? "" : files.get(0)) + ",";
	}

	/** @return The default Velocity root directory, which may set by an environment variable */
    protected String defaultVelocityRoot() {
        String envRoot = null;
        try {
            envRoot = System.getenv( VELOCITY_PATH_ENV_VAR );
        }
        catch (SecurityException ignore) {
            // not allowed to read the environment, no biggie
        }

        return (envRoot == null) ? DEFAULT_VELOCITY_ROOT_PATH : envRoot;
    }

    /** @return A properties object containing the configuration properties for this velocity instance */
    protected Properties getProperties( List<String> velocityPath ) {
        Properties p = new Properties();

        loadPropertiesFile( velocityPath, p );
        setDynamicProperties( velocityPath, p );

        return p;
    }

    /**
     * Set additional properties that are calculated based on current state
     * @param velocityPath The Velocity path as given
     * @param p The current Properties object
     */
    protected void setDynamicProperties( List<String> velocityPath, Properties p ) {
        if (p.getProperty( VELOCITY_FILE_RESOURCE_LOADER_PATH ) == null) {
            if (velocityPath.size() > 1) {
                for (int i = 0; i < velocityPath.size(); i++) {
                    velocityPath.set( i, velocityPath.get( i ).replaceFirst( "^file:", "" ) );
                }
                p.setProperty( VELOCITY_FILE_RESOURCE_LOADER_PATH, StringUtils.join( velocityPath, ", " ) );
            }
            else {
                String velocityURL = "";
                if (velocityPath.size() > 0) {
                    velocityURL = velocityPath.get( 0 );
                }

                if (velocityURL.matches( "^http:.*" )) {
                    p.setProperty( "url.resource.loader.root", velocityURL );
                }
                else {
                    p.setProperty( VELOCITY_FILE_RESOURCE_LOADER_PATH, velocityURL.replaceFirst( "^file:", "" ) );
                }

            }
        }
    }

    /**
     * Load the <code>velocity.properties</code> file from the Velocity root, if it exists.
     * @param velocityPath One or more Velocity root directories to look in
     * @param p Properties to instantiate
     */
    protected void loadPropertiesFile( List<String> velocityPath, Properties p ) {
        for (String velocityRoot: velocityPath) {
            InputStream is = EldaFileManager.get().open( velocityRoot + VELOCITY_PROPERTIES_FILE );

            if (is != null) {
                try {
                    p.load( is );
                }
                catch (IOException e) {
                    log.warn(ELog.message( "IO exception while reading properties: %s (%s)", e.getMessage(), e));
                    throw new WrappedIOException( e );
                }
                finally {
                    try {
                        is.close();
                    }
                    catch (IOException e) {
                        log.warn(ELog.message( "IO exception while closing properties input stream: %s (%s)", e.getMessage(), e));
                        throw new WrappedIOException( e );
                    }
                }

                // stop after we find the first velocity.properties
                break;
            }
        }
    }

    /** @return A new velocity context containing bindings that we will use to render the results */
    public VelocityContext createVelocityContext( Bindings binds ) {
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

    /**
    	Add the Elda bindings as context variables. Use getUnslashed to
    	unquote any quoted left braces (ie the {/ sequence) that were
    	introduced to quote raw string values eg esp from exception
    	messages.
    */
    protected void addBindingsToContext( VelocityContext vc, Bindings binds ) {
        for (String key: binds.keySet()) {
            vc.put( key, binds.getUnslashed( key ) );
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

	@Override public String getPoison() {
		return HTML_POISON;
	}

}
