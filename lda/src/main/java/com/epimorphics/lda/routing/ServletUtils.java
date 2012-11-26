package com.epimorphics.lda.routing;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.LARQManager;
import com.epimorphics.lda.support.MapMatching;
import com.epimorphics.lda.support.TDBManager;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Some methods useful in the two servlet-handling components
    of the routing classes.
*/
public class ServletUtils {

	
	static Logger log = LoggerFactory.getLogger( ServletUtils.class );
	
	public interface SpecContext {
		String getInitParameter( String name );
	}

	public static class ServletSpecContext implements ServletUtils.SpecContext {
		
		final HttpServlet underlying;
		
		ServletSpecContext(HttpServlet underlying) {
			this.underlying = underlying;
		}
		
		public String getInitParameter( String name ) {
			return underlying.getInitParameter( name );
		}
	}

	public static String withTrailingSlash(String path) {
	    return path.endsWith("/") ? path : path + "/";
	}

	public static String[] safeSplit(String s) {
	    return s == null || s.equals("") 
	    	? new String[] {} 
	    	: s.replaceAll( "[ \n\t]", "" ).split(",")
	    	;
	}

	public static Set<String> specNamesFromSystemProperties() {
		Properties p = System.getProperties();
		return MapMatching.allValuesWithMatchingKey( Container.ELDA_SPEC_SYSTEM_PROPERTY_NAME, p );
	}

	/**
	    If the prefix path is not null, update the root to have a
	    uriTemplatePrefix derived from the prefix path by substituting
	    {file} with the leafname of the file loaded from and (b) {api}
	    with the local name of the root.
	*/
	public static void setUriTemplatePrefix( String prefixPath, String filePath, Resource root) {
		if (prefixPath == null) return;
		String prefix = prefixPath
			.replaceAll( "\\{file\\}", "/" + new File(filePath).getName().replace( ".ttl", "" ) )
			.replaceAll( "\\{api\\}", "/" + root.getLocalName() )
			;
		root.addProperty( EXTRAS.uriTemplatePrefix, prefix );
	}

	public static void addLoadedFrom( Model m, String name ) {
		List<Statement> toAdd = new ArrayList<Statement>();
		List<Resource> apis = m
			.listStatements( null, RDF.type, API.API )
			.mapWith(Statement.Util.getSubject)
			.toList()
			;
		for (Resource api: apis) toAdd.add( m.createStatement( api, EXTRAS.loadedFrom, name ) );
		m.add( toAdd );
	}

	public static void loadSpecFromFile( ModelLoader ml, String prefixPath, String specPath ) {
		int chop = specPath.indexOf( "::" );
		if (chop >= 0) {
			// prefixPath :: fileName
			prefixPath = "/" + specPath.substring(0, chop);
			specPath = specPath.substring( chop + 2 );
		}
		log.info( "Loading spec file from " + specPath + " with prefix path " + prefixPath );
		Model init = ml.loadModel( specPath );
		addLoadedFrom( init, specPath );
		log.info( "Loaded " + specPath + ": " + init.size() + " statements" );
		registerModel( prefixPath, specPath, init );
	}

	/**
	 * Register all API endpoints specified in the given model with the
	 * router.
	 * @param model
	 */
	public static void registerModel( String prefixPath, String filePath, Model model ) {
	    for (ResIterator ri = model.listSubjectsWithProperty( RDF.type, API.API ); ri.hasNext();) {
	        Resource api = ri.next();
	        try {
	        	if (false) setUriTemplatePrefix( prefixPath, filePath, api );
	            SpecManagerFactory.get().addSpec( prefixPath, api.getURI(), "", model);
	        } catch (APISecurityException e) {
	            throw new APIException( "Internal error. Got security exception during bootstrap. Not possible!", e );
	        }
	    }
	}

	public static Set<String> specNamesFromInitParam(ServletSpecContext f) {
		return new HashSet<String>( Arrays.asList( safeSplit(f.getInitParameter( Container.INITIAL_SPECS_PARAM_NAME ) ) ) );
	}

	/**
	    The spec names can come from the init parameter set in the web.xml,
	    or they may preferentially be set from system properties. 
	
	 	@return 
	*/
	public static Set<String> getSpecNamesFromContext(ServletSpecContext f) {
		Set<String> found = specNamesFromSystemProperties();
		return found.size() > 0 ? found : specNamesFromInitParam(f);
	}

	public static String expandLocal( String baseFilePath, String given, String ifNull ) {
		String s = (given == null ? ifNull : given);
	    return s.replaceFirst( "^" + Container.LOCAL_PREFIX, baseFilePath );
	}

	public static void setupLARQandTDB( ServletSpecContext me ) {
	    String locStore = me.getInitParameter( "DATASTORE_KEY" );
	    String defaultTDB = locStore + "/tdb", defaultLARQ = locStore + "/larq";
	    String givenTDB = me.getInitParameter( TDBManager.TDB_BASE_DIRECTORY );
	    String givenLARQ =  me.getInitParameter( LARQManager.LARQ_DIRECTORY_KEY );
	    TDBManager.setBaseTDBPath( expandLocal( Loader.baseFilePath, givenTDB , defaultTDB ) );
	    LARQManager.setLARQIndexDirectory( expandLocal( Loader.baseFilePath, givenLARQ, defaultLARQ ) );
	}

}
