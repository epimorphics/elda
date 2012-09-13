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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.support.LARQManager;
import com.epimorphics.lda.support.TDBManager;
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
    	baseFilePath = ServletUtils.withTrailingSlash( getServletContext().getRealPath("/") );
    	log.info( "Starting Elda " + Version.string );
    	configureLog4J();
        log.info( "baseFilePath: " + baseFilePath );
    	String prefixPath = getInitParameter( Container.INITIAL_SPECS_PREFIX_PATH_NAME );
        setupLARQandTDB();
        modelLoader = new APIModelLoader( baseFilePath );
        FileManager.get().addLocatorFile( baseFilePath );
        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
        for (String spec : getSpecNamesFromContext()) {
             ServletUtils.loadSpecFromFile( modelLoader, prefixPath, spec );
        }
    }
    
    public void osgiInit(String filepath) {
        baseFilePath = filepath;
        modelLoader = new APIModelLoader(baseFilePath);
//        FileManager.get().addLocatorFile( baseFilePath );
//        modelLoader = new APIModelLoader(baseFilePath);
        FileManager.get().addLocatorFile( baseFilePath );
        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
    }

	/**
	    The spec names can come from the init parameter set in the web.xml,
	    or they may preferentially be set from system properties. 
	 
	 	@return 
	*/
	private Set<String> getSpecNamesFromContext() {
		Set<String> found = ServletUtils.specNamesFromSystemProperties();
		return found.size() > 0 ? found : specNamesFromInitParam();
	}

	private Set<String> specNamesFromInitParam() {
    	return new HashSet<String>( Arrays.asList( ServletUtils.safeSplit(getInitParameter( Container.INITIAL_SPECS_PARAM_NAME ) ) ) );
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

    public static final String DATASTORE_KEY = "com.epimorphics.api.dataStoreDirectory";

    private void setupLARQandTDB() {
        String locStore = getInitParameter( DATASTORE_KEY );
        String defaultTDB = locStore + "/tdb", defaultLARQ = locStore + "/larq";
        String givenTDB = getInitParameter( TDBManager.TDB_BASE_DIRECTORY );
        String givenLARQ =  getInitParameter( LARQManager.LARQ_DIRECTORY_KEY );
        TDBManager.setBaseTDBPath( expandLocal( givenTDB == null ? defaultTDB : givenTDB ) );
        LARQManager.setLARQIndexDirectory( expandLocal( givenLARQ == null ? defaultLARQ : givenLARQ ) );
    }

    private String expandLocal( String s ) {
//        return s.replaceFirst( "^" + LOCAL_PREFIX, baseFilePath );
//        Reg version blows up with a char out of range
        return s.replace( Container.LOCAL_PREFIX, baseFilePath );
    }
}

