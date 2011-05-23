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
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.FactoryConfigurationError;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIException;
import com.epimorphics.lda.core.APISecurityException;
import com.epimorphics.lda.core.ModelLoaderI;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.support.LARQManager;
import com.epimorphics.lda.support.MapMatching;
import com.epimorphics.lda.support.TDBManager;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

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

    public static final String INITIAL_SPECS_PARAM_NAME = "com.epimorphics.api.initialSpecFile";
    
    public static final String ELDA_SPEC_SYSTEM_PROPERTY_NAME = "elda.spec";

    public static final String LOG4J_PARAM_NAME = "log4j-init-file";

    /** prefix used to indicate file resources relative to the webapp root */
    public static final String LOCAL_PREFIX = "local:";

    protected static String baseFilePath = "";
    protected static String contextPath = "";

    protected static ModelLoaderI modelLoader;

    static Logger log = LoggerFactory.getLogger(Loader.class);

    public static String getBaseFilePath() {
        return baseFilePath;
    }
    
    public static String getContextPath() {
    	return contextPath;
    }

    @Override public void init() {
    	baseFilePath = withTrailingSlash( getServletContext().getRealPath("/") );
    	configureLog4J();
        log.info( "baseFilePath: " + baseFilePath );
        contextPath = getServletContext().getContextPath();
        setupLARQandTDB();
        modelLoader = new APIModelLoader(baseFilePath);
        FileManager.get().addLocatorFile( baseFilePath );
        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
        for (String spec : getSpecNamesFromContext()) {
             loadSpecFromFile(spec);
        }
    }
    
    public void osgiInit(String filepath) {
        baseFilePath = filepath;
        modelLoader = new APIModelLoader(baseFilePath);
        FileManager.get().addLocatorFile( baseFilePath );
    }

	public void loadSpecFromFile( String spec ) {
		Model init = getSpecModel( spec );
		String msg = "Loaded initial spec file from " + spec + " - " + init.size() + " statements";
		log.info( msg );
		registerModel( init );
	}

	/**
	    The spec names can come from the init parameter set in the web.xml,
	    or they may preferentially be set from system properties. 
	 
	 	@return 
	*/
	private Set<String> getSpecNamesFromContext() {
		Set<String> found = specNamesFromSystemProperties();
		return found.size() > 0 ? found : specNamesFromInitParam();
	}

	public Set<String> specNamesFromSystemProperties() {
		Properties p = System.getProperties();
		return MapMatching.allValuesWithMatchingKey( ELDA_SPEC_SYSTEM_PROPERTY_NAME, p );
	}

    private Set<String> specNamesFromInitParam() {
    	return new HashSet<String>( Arrays.asList( safeSplit(getInitParameter(INITIAL_SPECS_PARAM_NAME ) ) ) );
	}

	// Putting log4j.properties in the classes root as normal doesn't
    // seem to work in WTP even though it does for normal tomcat usage
    // This is an attempt to force logging configuration to be loaded
    private void configureLog4J() throws FactoryConfigurationError {
        String file = getInitParameter(LOG4J_PARAM_NAME);
        if (file == null) file = "log4j.properties"; // hackery
        if(file != null) {
            if (file.endsWith( ".xml" )) {
                DOMConfigurator.configure( baseFilePath + file );
            }
            else {
                PropertyConfigurator.configure(baseFilePath + file);
            }
        }
	}

    private String withTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private String[] safeSplit(String s) {
        return s.equals("") ? new String[] {} : s.split(",");
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
        return s.replace( LOCAL_PREFIX, baseFilePath );
    }

    private Model getSpecModel( String initialSpec ) {
        return modelLoader.loadModel( initialSpec );
    }

    /**
     * Register all API endpoints specified in the given model with the
     * router.
     * @param model
     */
    public static void registerModel(Model model) {
        for (ResIterator ri = model.listSubjectsWithProperty( RDF.type, API.API ); ri.hasNext();) {
            Resource api = ri.next();
            try {
                SpecManagerFactory.get().addSpec(api.getURI(), "", model);
            } catch (APISecurityException e) {
                throw new APIException( "Internal error. Got security exception duing bootstrap. Not possible!", e );
            }
        }
    }

    class APIModelLoader implements ModelLoaderI {

        String baseFilePathLocal;

        APIModelLoader(String base) {
            baseFilePathLocal = base;
        }

        @Override public Model loadModel(String uri) {
            log.info( "loadModel: " + uri );
            if (uri.startsWith(LOCAL_PREFIX)) {
                String specFile = "file:///" + baseFilePathLocal + uri.substring(LOCAL_PREFIX.length());
                return FileManager.get().loadModel( specFile );

            } else if (uri.startsWith( TDBManager.PREFIX )) {
                String modelName = uri.substring( TDBManager.PREFIX.length() );
                Model tdb = TDBManager.getTDBModelNamed( modelName );
                log.info( "get TDB model " + modelName );
                if (tdb.isEmpty()) log.warn( "the TDB model at " + modelName + " is empty -- has it been initialised?" );
                if (tdb.isEmpty()) throw new APIException( "the TDB model at " + modelName + " is empty -- has it been initialised?" );
                return tdb;

            } else {
                return FileManager.get().loadModel( uri );
            }
        }
    }

}

