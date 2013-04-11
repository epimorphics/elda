/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        Loader.java
    Created by:  Dave Reynolds
    Created on:  31 Jan 2010
 *
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.routing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.routing.ServletUtils.ServletSpecContext;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.sources.AuthMap.NamesAndValues;
import com.epimorphics.lda.specmanager.SpecManager;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.hp.hpl.jena.util.FileManager;

/**
 * This arranges for the current Api specifications to be
 * loaded and registered with the router.
 *
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class Loader extends HttpServlet {

	static {}
	
    private static final long serialVersionUID = 4184390033676415261L;

    protected static String baseFilePath = "";

    protected static ModelLoader modelLoader;

    static Logger log = LoggerFactory.getLogger(Loader.class);

    @Override public void init() {
    	ServletContext sc = getServletContext();   	
		baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
    	configureLog4J();
    	log.info( "Starting Elda " + Version.string );
        log.info( "baseFilePath: " + baseFilePath );
    	String prefixPath = getInitParameter( Container.INITIAL_SPECS_PREFIX_PATH_NAME );
        ServletUtils.setupLARQandTDB( new ServletSpecContext( this ) );
        modelLoader = new APIModelLoader( baseFilePath );
        FileManager.get().addLocatorFile( baseFilePath );
    //
        AuthMap am = AuthMap.loadAuthMap( FileManager.get(), wrapParameters() );
    //
        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
    //
        String contextName = adaptContextPath( sc.getContextPath() );
        for (String specTemplate : ServletUtils.getSpecNamesFromContext(new ServletUtils.ServletSpecContext(this))) {
        	String spec = specTemplate.replaceAll( "\\{APP\\}", contextName );
            ServletUtils.loadSpecsFromFiles( am, modelLoader, baseFilePath, prefixPath, spec );
        }
    }

    private String adaptContextPath(String contextPath) {
		return contextPath.equals("") ? "ROOT" : contextPath.substring(1);
	}

	/**
        Return a NamesAndValues which wraps the init parameters of this Loader
        servlet.
    */
	public NamesAndValues wrapParameters() {
		return new AuthMap.NamesAndValues() {

			@Override public String getParameter(String name) {
				return getInitParameter( name );
			}

			@Override public List<String> getParameterNames() {
				List<String> result = new ArrayList<String>();
				Enumeration<String> names = getInitParameterNames();
				while (names.hasMoreElements()) result.add( names.nextElement() );
				return result;
			}
        };
	}
    
    public void osgiInit(String filepath) {
        baseFilePath = filepath;
        modelLoader = new APIModelLoader(baseFilePath);
//        FileManager.get().addLocatorFile( baseFilePath );
//        modelLoader = new APIModelLoader(baseFilePath);
        FileManager.get().addLocatorFile( baseFilePath );
        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
    }

	// Putting log4j.properties in the classes root as normal doesn't
    // seem to work in WTP even though it does for normal tomcat usage
    // This is an attempt to force logging configuration to be loaded
	
    private void configureLog4J() throws FactoryConfigurationError {
        String file = getInitParameter(Container.LOG4J_PARAM_NAME);
        if (file == null) file = "log4j.properties";
        if (file != null) {
            if (file.endsWith( ".xml" )) {
                DOMConfigurator.configure( baseFilePath + file );
            }
            else {
                PropertyConfigurator.configure( baseFilePath + file);
            }
        }
	}
    
    /**
    	Get a new SpecManager implementation.
    */
    protected SpecManager getSpecManager() {
     	return new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader);
    }
    
}

