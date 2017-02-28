/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2017 Epimorphics Limited
*/

package com.epimorphics.lda.configs;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIFactory;
import com.epimorphics.lda.core.ModelLoader;
import com.epimorphics.lda.log.ELog;
import com.epimorphics.lda.routing.Router;
import com.epimorphics.lda.routing.ServletUtils;
import com.epimorphics.lda.specs.APISpec;
import com.epimorphics.lda.support.EldaFileManager;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
	ConfigLoader can load a single config file
*/
public class ConfigLoader {

	static Logger log = LoggerFactory.getLogger( ServletUtils.class );

	/**
		loadOneConfig file loads all of the configurations in the file named
		by 'thisSpecPath' into 'supplied router'. Loading is performed
		by 'ml' which interprets the spec path as it sees fit (usually 
		loading a plain text ttl file). Any prefix path applying to all
		configs is supplied in 'prefixPath' and the context path is
		'appName'.
	*/
	public static void loadOneConfigFile
		( Router router
		, String appName
		, ModelLoader ml
		, String prefixPath
		, String thisSpecPath
		) {    	
		
		// Loading the spec model and compiling it to an APISpec may fail.
		// We really don't want a spec file to be partially loaded, so
		// we build all the APISpecs first and let any exceptions bubble
		// up, and once we have the apiSpecs we load them into the router.
		
		log.info(ELog.message( "loading spec file from '%s' with prefix path '%s'", thisSpecPath, prefixPath));
		Model init = safeLoad(ml, thisSpecPath);
		ServletUtils.addLoadedFrom( init, thisSpecPath );
		log.info(ELog.message("loaded '%s' with %d statements", thisSpecPath, init.size()));
		
		List<StashEntry> entryList = new ArrayList<StashEntry>();
		
		List<Resource> roots = init.listSubjectsWithProperty( RDF.type, API.API ).toList();

		// Process each of the available config URIs and create the APISpec. If this
		// throws an exception, log a message and continue processing the remaining
		// configs.
		
		for (Resource specRoot: roots) {
	        try {
	        	APISpec apiSpec = new APISpec( prefixPath, appName, EldaFileManager.get(), specRoot, ml );
	        	entryList.add(new StashEntry(thisSpecPath, specRoot, apiSpec));	        	
	        } catch (RuntimeException e) {
	        	log.error(ELog.message("error processing config from file '%s': %s", thisSpecPath, e));
	        }
			
		}
		
		// Once we reach this point, we have compiled the spec and we only
		// need to inject it into the router (which cannot fail)  and
		// the stash (likewise). 
				
		LoadedConfigs.instance.unstash(thisSpecPath);
		
		for (StashEntry e: entryList) {
			LoadedConfigs.instance.stash(e.specPath,  e.URI, e.spec);
			APIFactory.registerApi(router, prefixPath, e.spec);
		}
	}

	/**
		safeLoad(ml, thisSpecPath) loads the configuration file thisSpecPath
		using the loader ml. If an exception is thrown while loading, an
		error message is logged and an empty model is returned.
	*/
	private static Model safeLoad(ModelLoader ml, String thisSpecPath) {
		try {
			return ml.loadModel( thisSpecPath );
		} catch (RuntimeException e) {
			log.error(ELog.message("error loading spec file '%s': %s", thisSpecPath, e));
			return ModelFactory.createDefaultModel();
		}
	}

}
