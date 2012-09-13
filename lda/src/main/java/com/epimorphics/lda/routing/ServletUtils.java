package com.epimorphics.lda.routing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.exceptions.APISecurityException;
import com.epimorphics.lda.specmanager.SpecManagerFactory;
import com.epimorphics.lda.support.MapMatching;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
    Some methods useful in the two servley-handling components
    of the routing classes.
*/
public class ServletUtils {

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

	public static void setPrefix( String prefixPath, String filePath, Resource root) {
		if (prefixPath == null) return;
		String prefix = prefixPath
			.replaceAll( "\\{file\\}", "/" + new File(filePath).getName().replace( ".ttl", "" ) )
			.replaceAll( "\\{api\\}", "/" + root.getLocalName() )
			;
		root.addProperty( EXTRAS.uriTemplatePrefix, prefix );
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
	        	setPrefix( prefixPath, filePath, api );
	            SpecManagerFactory.get().addSpec( api.getURI(), "", model);
	        } catch (APISecurityException e) {
	            throw new APIException( "Internal error. Got security exception duing bootstrap. Not possible!", e );
	        }
	    }
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
		Container.log.info( "Loading spec file from " + specPath );
		Model init = ml.loadModel( specPath );
		addLoadedFrom( init, specPath );
		Container.log.info( "Loaded " + specPath + ": " + init.size() + " statements" );
		registerModel( prefixPath, specPath, init );
	}

}
