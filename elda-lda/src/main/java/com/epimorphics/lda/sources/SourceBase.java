/**
    See lda-top/LICENCE (or https://raw.github.com/epimorphics/elda/master/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.sources;

import com.epimorphics.lda.exceptions.EldaException;
import com.epimorphics.lda.sources.Source.ResultSetConsumer;
import com.epimorphics.lda.textsearch.TextSearchConfig;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.Lock;

/**
    SourceBase provides the canonical implementation of the extended
    Source operations.
    
 	@author chris
*/

public abstract class SourceBase {
	
	private final TextSearchConfig textSearchConfig;
	
	public SourceBase() {
		this.textSearchConfig = new TextSearchConfig();
	}
	
	public SourceBase( Resource endpoint ) {
		this.textSearchConfig = new TextSearchConfig( endpoint );
	}
	
	public TextSearchConfig getTextSearchConfig() {
		return textSearchConfig;
	}
	
	/**
	    Each SourceBase subclass must provide <code>execute</code>.    
	*/
	public abstract QueryExecution execute( Query query );
	
	/**
	 	Each SourceBase subclass must provide a Lock on demand.
	*/
	public abstract Lock getLock();
	
	/**
	    <code>query</code> must be a DESCRIBE query. Answer the model
	    which is the description.
	*/
	public Model executeDescribe( Query query ) {
    	Lock l = getLock();
    	l.enterCriticalSection( Lock.READ );
    	QueryExecution qe = execute( query );
		try { 
			return qe.execDescribe(); 
		} finally {
			try { qe.close(); } finally { l.leaveCriticalSection(); } 
		}
	}
		
	/**
	    <code>query</code> must be a CONSTRUCT query. Answer the model
	    which is constructed.
	*/
	public Model executeConstruct( Query query ) {
    	Lock l = getLock();
    	l.enterCriticalSection( Lock.READ );
    	QueryExecution qe = execute( query );
		try { 
			return qe.execConstruct(); 
		}
		finally { 
			try { qe.close(); } finally { l.leaveCriticalSection(); } 
		}
	}

	/**
	    <code>q</code> must be a SELECT query. <code>c.setup()</code>
	    is called on the QueryExecution object to do any setup (ie,
	    LARQ index enabling). Then <code>c.consume</code> is supplied with 
	    the resultset from the select. When that returns, the execution
	    object is closed.
	*/
    public void executeSelect( Query q, ResultSetConsumer c ) {
    	Lock l = getLock();
    	if (l == null)EldaException.Broken
    		( "lock is null for " + this.toString() + " (" + this.getClass() + ")" );
    	l.enterCriticalSection( Lock.READ );
    	QueryExecution qe = execute( q );
    	try {    		
    		c.setup( qe );
    		c.consume( qe.execSelect() );
    	}
    	finally {			
    		try { qe.close(); } finally { l.leaveCriticalSection(); } 
    	}
    }
}
