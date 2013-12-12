/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
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

import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.restlets.RouterRestletSupport;
import com.epimorphics.lda.routing.ServletUtils.GetInitParameter;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.sources.AuthMap.NamesAndValues;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.vocabularies.ELDA;

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
    	ServletConfig fig = getServletConfig();
    	ServletContext sc = getServletContext();   	
		baseFilePath = ServletUtils.withTrailingSlash( sc.getRealPath("/") );
    	configureLog4J();
    	log.info( "\n\n  Starting Elda (Loader) " + Version.string + " " + ELDA.tag + "\n" );
        log.info( "baseFilePath: " + baseFilePath );
    	String prefixPath = getInitParameter( Container.INITIAL_SPECS_PREFIX_PATH_NAME );
        ServletUtils.setupLARQandTDB( sc );
        modelLoader = new APIModelLoader( baseFilePath );
        EldaFileManager.get().addLocatorFile( baseFilePath );
    //
        AuthMap am = AuthMap.loadAuthMap( EldaFileManager.get(), wrapParameters() );
    //
        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
    //
        String contextName = RouterRestletSupport.flatContextPath(sc.getContextPath());
        
        for (String specTemplate : ServletUtils.getSpecNamesFromContext(adaptConfig(fig))) {
        	String spec = specTemplate.replaceAll( "\\{APP\\}", contextName );
            ServletUtils.loadSpecsFromFiles( am, modelLoader, baseFilePath, prefixPath, spec );
        }
    }

    /**
     	We do this because for reasons that are not completely clear the
     	given ServletContext doesn't have the binding for INITIAL_SPECS_PARAM_NAME,
     	but the ServletConfig does.
    */
    private GetInitParameter adaptConfig(final ServletConfig fig) {
		return new GetInitParameter() {

			@Override public String getInitParameter(String name) {
				return fig.getInitParameter(name);
			}
		};
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
//        EldaFileManager.get().addLocatorFile( baseFilePath );
//        modelLoader = new APIModelLoader(baseFilePath);
        EldaFileManager.get().addLocatorFile( baseFilePath );
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
}

