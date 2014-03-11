package com.epimorphics.lda.restlets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.renderers.Renderer;
import com.epimorphics.lda.routing.*;
import com.epimorphics.lda.routing.ServletUtils.GetInitParameter;
import com.epimorphics.lda.routing.Container;
import com.epimorphics.lda.shortnames.CompleteContext;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.sources.AuthMap.NamesAndValues;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.specmanager.SpecManagerImpl;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.WrappedException;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 	Support methods and data structures for RouterRestlet.
*/
public class RouterRestletSupport {

    protected static Logger log = LoggerFactory.getLogger(RouterRestlet.class);

    public static class PrefixAndFilename {
    	final String prefixPath;
    	final String fileName;
    	
    	public PrefixAndFilename(String prefixPath, String fileName) {
    		this.prefixPath = prefixPath;
    		this.fileName = fileName;
    	}
    	
    	@Override public String toString() {
    		return "<" + prefixPath + " :: " + fileName + ">";
    	}
    }

	public static long latestConfigTime(ServletContext con, String contextPath) {
		long latestTime = 0;
		List<PrefixAndFilename> pfs = prefixAndFilenames( con, contextPath );
		for (PrefixAndFilename s: pfs) {
			long lmFile = new File(s.fileName).lastModified();
			long lmDir = new File(s.fileName).getParentFile().lastModified();
			long lm = lmFile > lmDir ? lmFile : lmDir;
			if (lm > latestTime) latestTime = lm;
		}
		return latestTime;
	}
    
    public static List<PrefixAndFilename> prefixAndFilenames( ServletContext con, String contextPath ) {
    	List<PrefixAndFilename> pfs = new ArrayList<PrefixAndFilename>();
		String baseFilePath = ServletUtils.withTrailingSlash( con.getRealPath("/") );
		Set<String> specFilenameTemplates = ServletUtils.getSpecNamesFromContext(adaptContext(con));
    	String givenPrefixPath = con.getInitParameter( Container.INITIAL_SPECS_PREFIX_PATH_NAME );
    //
    	log.debug( "configuration file templates: " + specFilenameTemplates );
    //
		for (String specTemplate: specFilenameTemplates) {
			String prefixName = givenPrefixPath;
			String specName = specTemplate.replaceAll( "\\{APP\\}" , contextPath );
			int separatorPos = specName.indexOf( "::" );
			if (separatorPos > -1) {
				prefixName = "/" + specName.substring(0, separatorPos);
				specName = specName.substring( separatorPos + 2 );
			}
			if (ServletUtils.isSpecialName( specName )) {
				pfs.add( new PrefixAndFilename( prefixName, specName ) );
			} else {
				String fullPath = specName.startsWith("/") ? specName : baseFilePath + specName;
				List<File> files = new Glob().filesMatching( fullPath );
				// log.info( "full path " + fullPath + " matches " + files.size() + " files." );
				for (File f: files) {
					String expandedPrefix = ServletUtils.containsStar(prefixName) ? ServletUtils.nameToPrefix(prefixName, specName, f.getName()) : prefixName;
					pfs.add( new PrefixAndFilename( expandedPrefix, f.getAbsolutePath() ) );
				}
			}				
		}
    	return pfs;
    }
    
    /**
        Create a new Router initialised with the configs appropriate to the
        contextPath.
    */
	public static Router createRouterFor( ServletContext con ) {
		String contextName = RouterRestletSupport.flatContextPath( con.getContextPath() );		
		List<PrefixAndFilename> pfs = prefixAndFilenames( con, contextName );
	//	
		Router result = new DefaultRouter();	
		String baseFilePath = ServletUtils.withTrailingSlash( con.getRealPath("/") );
        AuthMap am = AuthMap.loadAuthMap( EldaFileManager.get(), noNamesAndValues );
        ModelLoader modelLoader = new APIModelLoader( baseFilePath );
        addBaseFilepath( baseFilePath );
    //
        SpecManagerImpl sm = new SpecManagerImpl(result, modelLoader);
		SpecManagerFactory.set( sm );
	//		
		for (PrefixAndFilename pf: pfs) {
			loadOneConfigFile( result, am, modelLoader, pf.prefixPath, pf.fileName );
		}
		int count = result.countTemplates();
		return count == 0  ? RouterFactory.getDefaultRouter() : result;
	}

	/**
	    Add the baseFilePath to the FileManager singleton. Only do it
	    once, otherwise the instance will get larger on each config load
	    (at least that won't be once per query, though). Just possibly
	    there may be multiple servlet contexts so we add a new only only if 
	    its not already in the instance's locator list.
	*/
	private static void addBaseFilepath(String baseFilePath) {
		FileManager fm = EldaFileManager.get();
		for (Iterator<Locator> il = fm.locators(); il.hasNext();) {
			Locator l = il.next();
			if (l instanceof LocatorFile) 
				if (((LocatorFile) l).getName().equals(baseFilePath))
					return;
		}
		log.info( "adding locator for " + baseFilePath );
		EldaFileManager.get().addLocatorFile( baseFilePath );
	}

	private static GetInitParameter adaptContext(final ServletContext con) {
		return new GetInitParameter() {

			@Override public String getInitParameter(String name) {
				return con.getInitParameter(name);
			}
		};
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
				SpecManagerFactory.get().addSpec(prefixPath, am, prefixPath, api.getURI(), "", init );
			} catch (APISecurityException e) {
				throw new WrappedException(e);
			}
			APISpec apiSpec = new APISpec( prefixPath, am, EldaFileManager.get(), specRoot, ml );
			APIFactory.registerApi( router, prefixPath, apiSpec );
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
	
			@Override public BytesOut render(Times t, Bindings rc, Map<String, String> termBindings, APIResultSet results) {
				return r.render(t, rc, termBindings, results);
			}
	
			@Override public String getPreferredSuffix() {
				return r.getPreferredSuffix();
			}    
			
			@Override public CompleteContext.Mode getMode() {
				return r.getMode();
			}
		};
	}

	/**
        expiresAt (date/time in milliseconds) as an RFC1123 date/time string
        suitable for use in an HTTP header.
    */
    public static String expiresAtAsRFC1123(long expiresAt) {
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(expiresAt);
    	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
    	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    	String result = dateFormat.format(c.getTime());
    	
//    	System.err.println( ">> expires (RFC): " + result );
//    	long delta = expiresAt - System.currentTimeMillis();
//    	System.err.println( ">> expires in " + (delta/1000) + "s" );
    	
    	return result;
	}

}
