package com.epimorphics.lda.routing;

import java.util.*;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.Version;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.routing.ServletUtils.ServletSpecContext;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.hp.hpl.jena.util.FileManager;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Container extends ServletContainer {
	
	private static final long serialVersionUID = 1L;

	public static final String LOCAL_PREFIX = "local:";

	public static final String LOG4J_PARAM_NAME = "log4j-init-file";

	public static final String ELDA_SPEC_SYSTEM_PROPERTY_NAME = "elda.spec";

	public static final String INITIAL_SPECS_PARAM_NAME = "com.epimorphics.api.initialSpecFile";
	
	public static final String INITIAL_SPECS_PREFIX_PATH_NAME = "com.epimorphics.api.prefixPath";
	
	static Logger log = LoggerFactory.getLogger( Container.class );

    static Map<String, Router> routers = new HashMap<String, Router>();

	String baseFilePath = "";
	
	String prefixPath = "";
	
	Router router = null;
	
	ModelLoader modelLoader;

    @Override public void init() throws ServletException { 
    	super.init();
    	// configureLog4J();
    	String name = getServletName();
		log.info( "Starting servlet " + name + " for Elda " + Version.string );
    	baseFilePath = ServletUtils.withTrailingSlash( getServletContext().getRealPath( "/" ) );
    	modelLoader = new APIModelLoader( baseFilePath );
    	prefixPath = getInitParameter( INITIAL_SPECS_PREFIX_PATH_NAME );
    	routers.put( name,  router = new DefaultRouter() );
    	FileManager.get().addLocatorFile( baseFilePath );
    	ServletUtils.setupLARQandTDB( new ServletUtils.ServletSpecContext( this ) );
	//
    	loadModels( new ServletUtils.ServletSpecContext(this), router, modelLoader, prefixPath );
    }

	public static void loadModels( ServletSpecContext ssc, Router r, ModelLoader ml, String prefixPath ) {
		SpecManagerFactory.set( new SpecManagerImpl( r, ml ) );
    	for (String specPath : ServletUtils.getSpecNamesFromContext( ssc )) 
    		ServletUtils.loadSpecFromFile( ml, prefixPath, specPath );
	} 

    public void osgiInit(String filepath) {
        baseFilePath = filepath;
        modelLoader = new APIModelLoader(baseFilePath);
//        FileManager.get().addLocatorFile( baseFilePath );
//        modelLoader = new APIModelLoader(baseFilePath);
        FileManager.get().addLocatorFile( baseFilePath );
        SpecManagerFactory.set( new SpecManagerImpl( RouterFactory.getDefaultRouter(), modelLoader) );
    }

//	// Putting log4j.properties in the classes root as normal doesn't
//    // seem to work in WTP even though it does for normal tomcat usage
//    // This is an attempt to force logging configuration to be loaded
//    private void configureLog4J() throws FactoryConfigurationError {
//        String file = getInitParameter(LOG4J_PARAM_NAME);
//        if (file == null) file = "log4j.properties";
//        if (file != null) {
//            if (file.endsWith( ".xml" )) {
//                DOMConfigurator.configure( baseFilePath + file );
//            }
//            else {
//                PropertyConfigurator.configure(baseFilePath + file);
//            }
//        }
//	}

    public static Router routerForServlet( String name ) {
		return routers.get(name);
	}

}
