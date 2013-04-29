package com.epimorphics.lda.restlets;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.APIFactory;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.routing.APIModelLoader;
import com.epimorphics.lda.routing.Container;
import com.epimorphics.lda.routing.DefaultRouter;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.routing.RouterFactory;
import com.epimorphics.lda.routing.ServletUtils;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.sources.AuthMap.NamesAndValues;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.Glob;
import com.epimorphics.lda.support.Times;
import com.epimorphics.util.MediaType;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 	Support methods and data structures for RouterRestlet.
*/
public class RouterRestletSupport {

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    /**
        Create a new Router initialised with the configs appropriate to the
        contextPath.
    */
	public static Router createRouterFor( ServletConfig sc, String contextName ) {
		Router result = new DefaultRouter();
		
		String baseFilePath = ServletUtils.withTrailingSlash( sc.getServletContext().getRealPath("/") );
		
        AuthMap am = AuthMap.loadAuthMap( FileManager.get(), noNamesAndValues );
        
        ModelLoader modelLoader = new APIModelLoader( baseFilePath );
        
//        SpecManagerFactory.set( new SpecManagerImpl(RouterFactory.getDefaultRouter(), modelLoader) );
        SpecManagerImpl sm = new SpecManagerImpl(result, modelLoader);
		SpecManagerFactory.set( sm );

    	String prefixPath = sc.getInitParameter( Container.INITIAL_SPECS_PREFIX_PATH_NAME );
		
		Set<String> specFilenameTemplates = ServletUtils.getSpecNamesFromContext(new ServletConfigSpecContext(sc));
		
		for (String specTemplate: specFilenameTemplates) {
			String specName = specTemplate.replaceAll( "\\{APP\\}" , contextName );
			String prefixPath1 = prefixPath;
			String specPath = specName;
			int chop = specPath.indexOf( "::" );
				if (chop >= 0) {
					// prefixPath :: fileName
					prefixPath1 = "/" + specPath.substring(0, chop);
					specPath = specPath.substring( chop + 2 );
				}
			//	
				if (ServletUtils.isSpecialName(specPath)) {
					loadOneConfigFile( result, am, modelLoader, prefixPath1, specPath );
				} else {
					String fullPath = specPath.startsWith("/") ? specPath : baseFilePath + specPath;
					List<File> files = new Glob().filesMatching( fullPath );
					log.info( "Found " + files.size() + " file(s) matching specPath " + specPath );
					for (File f: files) {
						String pp = ServletUtils.containsStar(prefixPath1) ? ServletUtils.nameToPrefix(prefixPath1, specPath, f.getName()) : prefixPath1;
						loadOneConfigFile(result, am, modelLoader, pp, f.getAbsolutePath());
					}
				}
		}
		int count = result.countTemplates();
		return count == 0  ? RouterFactory.getDefaultRouter() : result;
	}

	public static String flatContextPath(String contextPath) {
		return contextPath.equals("") ? "ROOT" : contextPath.substring(1).replaceAll("/", "_");
	}

	public static void loadOneConfigFile(Router router, AuthMap am, ModelLoader ml, String prefixPath, String thisSpecPath) {
		log.info( "Loading spec file from " + thisSpecPath + " with prefix path " + prefixPath );
		Model init = ml.loadModel( thisSpecPath );
		ServletUtils.addLoadedFrom( init, thisSpecPath );
		log.info( "Loaded " + thisSpecPath + ": " + init.size() + " statements" );
		for (ResIterator ri = init.listSubjectsWithProperty( RDF.type, API.API ); ri.hasNext();) {
		    Resource api = ri.next();
            Resource specRoot = init.getResource(api.getURI());
            try {
				SpecManagerFactory.get().addSpec(am, prefixPath, api.getURI(), "", init );
			} catch (APISecurityException e) {
				throw new WrappedException(e);
			}
			APISpec apiSpec = new APISpec( am, FileManager.get(), specRoot, ml );
			APIFactory.registerApi( router, prefixPath, apiSpec );
		}
	}
	
    static class ServletConfigSpecContext implements ServletUtils.SpecContext {

    	protected final ServletConfig config;
    	
		public ServletConfigSpecContext(ServletConfig config) {
			this.config = config;
		}

		@Override public String getInitParameter(String name) {
			return config.getInitParameter(name);
		}
    }
    
    static final NamesAndValues noNamesAndValues = new NamesAndValues() {

		@Override public String getParameter(String name) {
			return null;
		}

		@Override public List<String> getParameterNames() {
			return Arrays.asList( new String[]{} );
		}
    	
    };
    

	/**
	    Given a renderer r and a media type mt, return a new renderer which
	    behaves like r except that it announces its media type as mt. r
	    itself is not changed.
	    
	    This code should be somewhere more sensible. In fact the whole
	    renderer-choosing machinery needs a good cleanup.
	*/
	protected static Renderer changeMediaType( final Renderer r, final MediaType mt ) {
		return new Renderer() {
	
			@Override public MediaType getMediaType(Bindings unused) {
				return mt;
			}
	
			@Override public BytesOut render(Times t, Bindings rc, APIResultSet results) {
				return r.render(t, rc, results);
			}
	
			@Override public String getPreferredSuffix() {
				return r.getPreferredSuffix();
			}    		
		};
	}

}
