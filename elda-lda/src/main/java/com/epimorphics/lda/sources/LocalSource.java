/*
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        LocalSource.java
    Created by:  Dave Reynolds
    Created on:  5 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.sources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.lda.exceptions.APIException;
import com.epimorphics.lda.vocabularies.API;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.FileManager;

/**
 	Data source which represents an in-memory model loaded
 	from a local file. Used for testing. Model will be reloaded
 	from file each time this class is constructed!
 	
 	@author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 	@version $Revision: $
*/
public class LocalSource extends SourceBase implements Source {
    
    static Logger log = LoggerFactory.getLogger(LocalSource.class);

    public static final String PREFIX = "local:";
    
    protected final Model source; 
    protected final String endpoint;
    
    protected final Dataset sourceDataset = DatasetFactory.createMem(); 
    
    public LocalSource( FileManager fm, Resource endpoint ) {
    	super( endpoint );
    	
    	String endpointString = endpoint.getURI();
        if (!endpointString.startsWith(PREFIX))
            throw new APIException("Illegal local endpoint: " + endpointString);
        
        String [] fileNames = endpointString.substring( PREFIX.length() ).split(",");
        String fileName = fileNames[0];

    // NOTE that some tests are sensitive to the order that items
    // turn up in. This way of setting up the dataset's default model
    // means that the same ordering is observed as if we were just using
    // a source model rather than a dataset. Horrible: TODO remove this
    // fragility.
        Model dsm = this.sourceDataset.getDefaultModel();
        this.source = dsm;
        fm.readModel(dsm, fileName);
        String baseName = fileName;
    //
        for (int i = 1; i < fileNames.length; i += 1) {
        	String leafname = fileNames[i];
        	String name = (leafname.startsWith("...") ? adjust(baseName, leafname) : leafname);
        	Model named = fm.loadModel(name);
        	baseName = name;
        	sourceDataset.addNamedModel("file:///" + name, named);
        }
    //
        this.endpoint = endpointString;
    }
    
    private String adjust(String baseName, String leafname) {
    	String tailName = leafname.substring(3);
    	int slash = baseName.lastIndexOf("/");
		return slash < 0 ? tailName : baseName.substring(0, slash + 1) + tailName;
	}

	@Override public QueryExecution execute(Query query) {
        if (log.isDebugEnabled()) log.debug("Running query: " + query);
        return QueryExecutionFactory.create(query, sourceDataset);
    }
    
    @Override public Lock getLock() {
    	return sourceDataset.getLock();
    }
    
    @Override public String toString() {
        return "Local datasource - " + endpoint;
    }
    
    /**
     	Add metadata describing this source to a metdata model 
    */
    @Override public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, ResourceFactory.createResource(endpoint));
    }

    /**
        Local sources are held in in-memory models and support nested selects
        via ARQ.
    */
	@Override public boolean supportsNestedSelect() {
		return true;
	}

}

