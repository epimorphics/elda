/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2017 Epimorphics Limited
*/

package com.epimorphics.lda.configs;

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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
	ConfigLoader can load a single config file
*/
public class ConfigLoader {

	static Logger log = LoggerFactory.getLogger( ServletUtils.class );

	public static void loadOneConfigFile
		( Router router
		, String appName
		, ModelLoader ml
		, String prefixPath
		, String thisSpecPath
		) {    	
		log.info(ELog.message( "loading spec file from '%s' with prefix path '%s'", thisSpecPath, prefixPath));
		Model init = ml.loadModel( thisSpecPath );
		ServletUtils.addLoadedFrom( init, thisSpecPath );
		log.info(ELog.message("loaded '%s' with %d statements", thisSpecPath, init.size()));
		
		LoadedConfigs.instance.unstash(thisSpecPath);

		List<Resource> roots = init.listSubjectsWithProperty( RDF.type, API.API ).toList();

		for (Resource specRoot: roots) {
	        
			APISpec apiSpec = new APISpec( prefixPath, appName, EldaFileManager.get(), specRoot, ml );
			APIFactory.registerApi( router, prefixPath, apiSpec );
			
			LoadedConfigs.instance.stash(thisSpecPath, specRoot, apiSpec);
		}
	}

}
