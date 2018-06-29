/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.

    (c) Copyright 2017 Epimorphics Limited
*/

package com.epimorphics.lda.configs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.riot.RiotException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.core.APIFactory;
import com.epimorphics.lda.core.ModelLoader;
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
		
		log.info("loading spec file from '{}' with prefix path '{}'", thisSpecPath, prefixPath);
		Model init = safeLoad(thisSpecPath);
		ServletUtils.addLoadedFrom( init, thisSpecPath );
		log.info("loaded '{}' with {} statements", thisSpecPath, init.size());
		
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
	        	log.error("error processing config from file '{}': {}", thisSpecPath, e);
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

	public static Model loadModelExpanding(String path) {
		return safeLoad(path);
	}
	
	/**
		safeLoad(ml, thisSpecPath) loads the configuration file thisSpecPath
		using the loader ml. If an exception is thrown while loading, an
		error message is logged and an empty model is returned.
	*/
	private static Model safeLoad(String thisSpecPath) {
		IncludeReader r = new IncludeReader(thisSpecPath);
		try {
			Model m = ModelFactory.createDefaultModel();
			return m.read(r, "", "TTL");
		} catch (RiotException re) {
			String message = re.getMessage();
			Pattern p = Pattern.compile("line: ([0-9]+)");
			Matcher mat = p.matcher(message);
			if (!mat.find()) throw re;
		//
			int intLine = Integer.parseInt(mat.group(1));
			Position where = r.mapLine(intLine);
			throw new RuntimeException("on line " + where.lineNumber + " of " + where.pathName, re);
		
		} catch (RuntimeException e) {
			// e.printStackTrace(System.err);
			log.error("error loading spec file '{}': {}", thisSpecPath, e);
			return ModelFactory.createDefaultModel();
			
		} finally {
			try {
				r.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			} 			
		}
	}
	
//	private static void loadModelExpanding(ModelLoader ml, StringBuilder result, String path, Map<String, String> seen) {
//		String already = seen.get(path);
//		if (already == null) {
//			String fileContent = EldaFileManager.get().readWholeFileAsUTF8(path);
//			int here = 0;
//			while (true) {
//				int foundAt = fileContent.indexOf("#include ", here);
//				if (foundAt < 0) break;
//				String leadingContent = fileContent.substring(here, foundAt);
//				result.append(leadingContent);
//				int atNL = fileContent.indexOf('\n', foundAt);
//				String foundPath = fileContent.substring(foundAt + 9, atNL);				
//				File sibling = new File(new File(path).getParent(), foundPath);
//				String fullPath = foundPath.startsWith("/") ? foundPath : sibling.toString(); 				
//				loadModelExpanding(ml, result, fullPath, seen);		
//				here = atNL + 1;
//			}
//			result.append(fileContent.substring(here));
//		} else {
//			result.append(already);
//		}
//	}
	

}
