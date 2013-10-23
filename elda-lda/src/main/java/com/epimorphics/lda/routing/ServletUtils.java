package com.epimorphics.lda.routing;

import java.io.File;
import java.util.*;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.sources.AuthMap;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.*;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Some methods useful in the two servlet-handling components
    of the routing classes.
*/
public class ServletUtils {

	static Logger log = LoggerFactory.getLogger( ServletUtils.class );

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

	public static void loadSpecsFromFiles( AuthMap am, ModelLoader ml, String baseFilePath, String prefixPath, String specPath ) {
		int chop = specPath.indexOf( "::" );
		if (chop >= 0) {
			// prefixPath :: fileName
			prefixPath = "/" + specPath.substring(0, chop);
			specPath = specPath.substring( chop + 2 );
		}
	//	
		if (isSpecialName(specPath)) {
			loadOneConfigFile( am, ml, prefixPath, specPath );
		} else {
			String fullPath = specPath.startsWith("/") ? specPath : baseFilePath + specPath;
			List<File> files = new Glob().filesMatching( fullPath );
			log.info( "Found " + files.size() + " file(s) matching specPath " + specPath );
			for (File f: files) {
				String pp = containsStar(prefixPath) ? nameToPrefix(prefixPath, specPath, f.getName()) : prefixPath;
				loadOneConfigFile(am, ml, pp, f.getAbsolutePath());
			}
		}
	}

	public static boolean containsStar(String prefixPath) {
		return prefixPath == null ? false : prefixPath.contains("*");
	}

	/**
	    nameToPrefix matches the last segment of the pathname <code>specPath</code>
	    against the leafname <code>name</code> and replaces any '*' character 
	    in <code>wildPrefix</code> with the matched wildcard part(s) (joined
	    if necessary by the character '-') from the match, returning the
	    modified result.
	*/
	public static String nameToPrefix(String wildPrefix, String specPath, String name) {
		String wildPart = new File(specPath).getName();
		String matched = new Glob().extract( wildPart, "-", name );
		return wildPrefix.replace( "*", (matched == null ? "NOMATCH" : matched) );
	}

	public static boolean isSpecialName( String specPath ) {
		return specPath.startsWith( Container.LOCAL_PREFIX ) 
			|| specPath.startsWith( TDBManager.PREFIX )
			;
	}

	public static void loadOneConfigFile(AuthMap am, ModelLoader ml, String prefixPath, String thisSpecPath) {
		log.info( "Loading spec file from " + thisSpecPath + " with prefix path " + prefixPath );
		Model init = ml.loadModel( thisSpecPath );
		addLoadedFrom( init, thisSpecPath );
		log.info( "Loaded " + thisSpecPath + ": " + init.size() + " statements" );
		registerModel( am, prefixPath, thisSpecPath, init );
	}

	/**
	 * Register all API endpoints specified in the given model with the
	 * router.
	 * @param model
	 */
	public static void registerModel( AuthMap am, String prefixPath, String filePath, Model model ) {
	    for (ResIterator ri = model.listSubjectsWithProperty( RDF.type, API.API ); ri.hasNext();) {
	        Resource api = ri.next();
	        try {
	        	if (false) setUriTemplatePrefix( prefixPath, filePath, api );
	            SpecManagerFactory.get().addSpec( prefixPath, am, prefixPath, api.getURI(), "", model);
	        } catch (APISecurityException e) {
	            throw new APIException( "Internal error. Got security exception during bootstrap. Not possible!", e );
	        }
	    }
	}
	
	public interface GetInitParameter {
		public String getInitParameter(String name);
	}

	public static Set<String> specNamesFromInitParam( GetInitParameter f ) {
		String specString = f.getInitParameter( Container.INITIAL_SPECS_PARAM_NAME );
		return new HashSet<String>( Arrays.asList( safeSplit(specString) ) );
	}

	/**
	    The spec names can come from the init parameter set in the web.xml,
	    or they may preferentially be set from system properties. 
	
	 	@return 
	*/
	public static Set<String> getSpecNamesFromContext(GetInitParameter f) {
		Set<String> found = specNamesFromSystemProperties();
		return found.size() > 0 ? found : specNamesFromInitParam(f);
	}

	public static String expandLocal( String baseFilePath, String given, String ifNull ) {
		String s = (given == null ? ifNull : given);
	    return s.replaceFirst( "^" + Container.LOCAL_PREFIX, baseFilePath );
	}

	public static void setupLARQandTDB( ServletContext me ) {
	    String locStore = me.getInitParameter( "DATASTORE_KEY" );
	    String defaultTDB = locStore + "/tdb";
	    String givenTDB = me.getInitParameter( TDBManager.TDB_BASE_DIRECTORY );
	    TDBManager.setBaseTDBPath( expandLocal( Loader.baseFilePath, givenTDB , defaultTDB ) );
	}

}
